package com.pgf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminAuthService {

    @Value("${app.admin.password:pgf-admin-2025}")
    private String adminPassword;

    public void authenticate(String password) {
        if (!adminPassword.equals(password)) {
            log.warn("Failed admin login attempt");
            throw new BadCredentialsException("Invalid password");
        }
        log.info("Admin authentication successful");
    }
}