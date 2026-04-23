package com.pgf.repository;

import com.pgf.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {
    Optional<AdminUser> findByEmail(String email);
    Optional<AdminUser> findByInvitationToken(String invitationToken);
    boolean existsByDisplayNameIgnoreCase(String displayName);
}