package com.pgf.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseJwtFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        log.info(">>> SupabaseJwtFilter hit: {} {}", request.getMethod(), request.getRequestURI());
        String path = request.getRequestURI();
        boolean isProtected = path.startsWith("/api/admin/")
                && !path.equals("/api/admin/auth/login")
                && !path.equals("/api/admin/auth/register")
                && !path.startsWith("/api/admin/auth/approve/");

        if (!isProtected) {
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing authorization");
            return;
        }

        if (header.startsWith("Bearer ")) {
            handleJwt(header.substring(7), response, chain, request);
        } else if (header.startsWith("Basic ")) {
            handleBasic(header.substring(6), response, chain, request);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authorization");
        }
    }

    private void handleJwt(String token, HttpServletResponse response,
                           FilterChain chain, HttpServletRequest request) throws IOException, ServletException {
        try {
            JsonNode payload = decodeJwtPayload(token);
            boolean approved = payload.path("user_metadata").path("approved").asBoolean(false);
            if (!approved) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account pending approval");
                return;
            }
            String userId = payload.path("sub").asText();
            setAuthentication(userId);
        } catch (Exception e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }
        chain.doFilter(request, response);
    }

    private void handleBasic(String encoded, HttpServletResponse response,
                             FilterChain chain, HttpServletRequest request) throws IOException, ServletException {
        try {
            String decoded = new String(Base64.getDecoder().decode(encoded));
            String password = decoded.contains(":") ? decoded.split(":", 2)[1] : decoded;
            if (adminPassword.equals(password)) {
                setAuthentication("admin-legacy");
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
                return;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            return;
        }
        chain.doFilter(request, response);
    }

    private void setAuthentication(String principal) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private JsonNode decodeJwtPayload(String token) throws IOException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT structure");
        }
        return objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
    }
}