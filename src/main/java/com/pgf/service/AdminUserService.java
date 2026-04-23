package com.pgf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pgf.model.AdminUser;
import com.pgf.repository.AdminUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminUserService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AdminUserRepository adminUserRepository;
    private final GmailNotificationService gmailNotificationService;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String supabaseServiceKey;

    public void sendInvitation(String email) {
        AdminUser pending = adminUserRepository.findByEmail(email).orElse(null);

        if (pending != null && pending.getApproved()) {
            throw new IllegalArgumentException("Un compte actif existe déjà pour cet e-mail.");
        }

        if (pending == null) {
            pending = new AdminUser();
            pending.setId(UUID.randomUUID());
            pending.setEmail(email);
        }

        String token = UUID.randomUUID().toString();
        pending.setInvitationToken(token);
        pending.setInvitationSentAt(LocalDateTime.now());
        adminUserRepository.save(pending);

        gmailNotificationService.sendInvitation(email, token);
    }

    public void registerWithToken(String token, String password, String displayName) {
        if (adminUserRepository.existsByDisplayNameIgnoreCase(displayName)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris.");
        }

        AdminUser pending = adminUserRepository.findByInvitationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token d'invitation invalide ou expiré."));

        HttpHeaders headers = supabaseHeaders();
        ObjectNode body = objectMapper.createObjectNode()
                .put("email", pending.getEmail())
                .put("password", password)
                .put("email_confirm", false);
        body.putObject("user_metadata")
                .put("display_name", displayName)
                .put("approved", false);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/admin/users",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    JsonNode.class
            );

            JsonNode user = response.getBody();
            if (user == null || !user.has("id")) {
                throw new IllegalStateException("Supabase user creation failed");
            }

            String email = pending.getEmail();
            adminUserRepository.delete(pending);
            adminUserRepository.flush();

            AdminUser adminUser = new AdminUser();
            adminUser.setId(UUID.fromString(user.get("id").asText()));
            adminUser.setEmail(email);
            adminUser.setDisplayName(displayName);
            adminUserRepository.save(adminUser);

            gmailNotificationService.sendAdminApprovalRequest(adminUser.getId().toString(), email, displayName);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 422) {
                throw new IllegalArgumentException("Un compte avec cette adresse e-mail existe déjà.");
            }
            throw e;
        }
    }

    public void approveUser(String userId) {
        AdminUser adminUser = adminUserRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found: " + userId));

        HttpHeaders headers = supabaseHeaders();
        ObjectNode body = objectMapper.createObjectNode();
        body.put("email_confirm", true);
        body.putObject("user_metadata").put("approved", true);

        restTemplate.exchange(
                supabaseUrl + "/auth/v1/admin/users/" + userId,
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                JsonNode.class
        );

        adminUser.setApproved(true);
        adminUser.setApprovedAt(LocalDateTime.now());
        adminUserRepository.save(adminUser);

        gmailNotificationService.sendApprovalConfirmation(adminUser.getEmail(), adminUser.getDisplayName());
        log.info("Admin user approved: {}", userId);
    }

    private HttpHeaders supabaseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(supabaseServiceKey);
        headers.set("apikey", supabaseServiceKey);
        return headers;
    }
}