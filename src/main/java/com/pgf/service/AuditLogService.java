package com.pgf.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgf.model.AuditLog;
import com.pgf.repository.AuditLogRepository;
import com.pgf.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private static final String ADMIN_NAME_HEADER = "X-Admin-Name";
    private static final String FALLBACK_ACTOR = "admin-legacy";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void logCreate(String entityType, Long entityId, Object created) {
        save(entityType, entityId, "CREATE", null, toJson(created));
    }

    public void logUpdate(String entityType, Long entityId, Object before, Object after) {
        Map<String, Object> beforeFields = toMap(before);
        Map<String, Object> afterFields = toMap(after);
        save(entityType, entityId, "UPDATE",
                toJson(changedFields(beforeFields, afterFields)),
                toJson(changedFields(afterFields, beforeFields)));
    }

    public void logDelete(String entityType, Long entityId, Object deleted) {
        save(entityType, entityId, "DELETE", toJson(deleted), null);
    }

    private void save(String entityType, Long entityId, String action, String beforeJson, String afterJson) {
        HttpServletRequest request = currentRequest();
        auditLogRepository.save(AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .beforeJson(beforeJson)
                .afterJson(afterJson)
                .performedBy(currentActor(request))
                .ipAddress(request == null ? null : RequestUtils.extractIp(request))
                .userAgent(request == null ? null : RequestUtils.extractUserAgent(request))
                .build());
    }

    private Map<String, Object> changedFields(Map<String, Object> source, Map<String, Object> reference) {
        Map<String, Object> changed = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (!Objects.equals(Objects.toString(value, null), Objects.toString(reference.get(key), null))) {
                changed.put(key, value);
            }
        });
        return changed;
    }

    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.convertValue(value, new TypeReference<>() {});
    }

    private String toJson(Object value) {
        if (value == null || (value instanceof Map<?, ?> map && map.isEmpty())) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit payload", e);
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    private String currentActor(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && StringUtils.hasText(authentication.getName())) {
            return authentication.getName();
        }
        String headerName = request == null ? null : request.getHeader(ADMIN_NAME_HEADER);
        return StringUtils.hasText(headerName) ? headerName : FALLBACK_ACTOR;
    }
}
