package com.pgf.controller;

import com.pgf.dto.AdminUserDto;
import com.pgf.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@Tag(name = "Admin Auth")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Value("${app.admin.register-secret}")
    private String registerSecret;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String secret, @RequestBody AdminUserDto dto) {
        if (!registerSecret.equals(secret)) {
            return ResponseEntity.status(403).build();
        }
        try {
            adminUserService.registerPendingUser(dto.email(), dto.password(), dto.displayName());
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @GetMapping("/approve/{userId}")
    @Operation(summary = "Approve admin user")
    public ResponseEntity<String> approve(@PathVariable String userId) {
        adminUserService.approveUser(userId);
        return ResponseEntity.ok("Utilisateur approuvé. Un e-mail de confirmation lui a été envoyé.");
    }
}