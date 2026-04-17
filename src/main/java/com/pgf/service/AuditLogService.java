package com.pgf.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgf.model.AuditLog;
import com.pgf.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(String entityType, Long entityId, String action,
                    Object before, Object after,
                    String performedBy, String ipAddress, String userAgent) {
        try {
            String beforeJson = null;
            String afterJson = null;

            if ("UPDATE".equals(action) && before != null && after != null) {
                Map<String, Object> beforeMap = toMap(before);
                Map<String, Object> afterMap = toMap(after);

                Map<String, Object> changedBefore = beforeMap.entrySet().stream()
                        .filter(e -> !objectEquals(e.getValue(), afterMap.get(e.getKey())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                Map<String, Object> changedAfter = afterMap.entrySet().stream()
                        .filter(e -> !objectEquals(e.getValue(), beforeMap.get(e.getKey())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                beforeJson = changedBefore.isEmpty() ? null : objectMapper.writeValueAsString(changedBefore);
                afterJson = changedAfter.isEmpty() ? null : objectMapper.writeValueAsString(changedAfter);
            } else if ("CREATE".equals(action) && after != null) {
                afterJson = objectMapper.writeValueAsString(toMap(after));
            } else if ("DELETE".equals(action) && before != null) {
                beforeJson = objectMapper.writeValueAsString(toMap(before));
            }

            AuditLog entry = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .beforeJson(beforeJson)
                    .afterJson(afterJson)
                    .performedBy(performedBy)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            auditLogRepository.save(entry);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log for {}/{}", entityType, entityId, e);
        }
    }

    private Map<String, Object> toMap(Object obj) throws JsonProcessingException {
        return objectMapper.convertValue(obj, new TypeReference<>() {});
    }

    private boolean objectEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.toString().equals(b.toString());
    }
}