package com.pgf.controller;

import com.pgf.dto.AdminUserDto;
import com.pgf.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/invite")
    @Operation(summary = "Send invitation email to a new admin user")
    public ResponseEntity<Void> invite(@RequestBody AdminUserDto dto) {
        adminUserService.sendInvitation(dto.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String token, @RequestBody AdminUserDto dto) {
        try {
            adminUserService.registerWithToken(token, dto.password(), dto.displayName());
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