package com.pgf.repository;

import com.pgf.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {}