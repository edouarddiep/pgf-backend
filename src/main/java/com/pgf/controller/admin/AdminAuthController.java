package com.pgf.controller.admin;

import com.pgf.dto.AdminUserDto;
import com.pgf.service.AdminAuthService;
import com.pgf.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "Admin - Auth", description = "Admin authentication and enrolment")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminUserService adminUserService;

    @PostMapping("/login")
    @Operation(summary = "Admin login")
    public void login(@RequestBody AdminLoginRequest request) {
        adminAuthService.authenticate(request.password());
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Send an invitation e-mail to a new admin user")
    public void invite(@RequestBody AdminUserDto dto) {
        adminUserService.sendInvitation(dto.email());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Register an invited admin user")
    public void register(@RequestParam String token, @RequestBody AdminUserDto dto) {
        adminUserService.registerWithToken(token, dto.password(), dto.displayName());
    }

    @GetMapping("/approve/{userId}")
    @Operation(summary = "Approve an admin user")
    public String approve(@PathVariable String userId) {
        adminUserService.approveUser(userId);
        return "Utilisateur approuvé. Un e-mail de confirmation lui a été envoyé.";
    }

    public record AdminLoginRequest(String password) {}
}
