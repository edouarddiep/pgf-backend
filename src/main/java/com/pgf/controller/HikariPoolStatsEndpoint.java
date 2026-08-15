package com.pgf.controller;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/hikari")
@Tag(name = "Admin - Monitoring", description = "Connection pool diagnostics")
@RequiredArgsConstructor
public class HikariPoolStatsEndpoint {

    private final DataSource dataSource;

    @GetMapping("/stats")
    @Operation(summary = "Get HikariCP pool statistics")
    public Map<String, Object> getStats() {
        if (!(dataSource instanceof HikariDataSource hikariDataSource)) {
            return Map.of();
        }
        HikariPoolMXBean pool = hikariDataSource.getHikariPoolMXBean();

        return Map.of(
                "activeConnections", pool.getActiveConnections(),
                "idleConnections", pool.getIdleConnections(),
                "totalConnections", pool.getTotalConnections(),
                "threadsAwaitingConnection", pool.getThreadsAwaitingConnection(),
                "maximumPoolSize", hikariDataSource.getMaximumPoolSize(),
                "minimumIdle", hikariDataSource.getMinimumIdle(),
                "connectionTimeout", hikariDataSource.getConnectionTimeout(),
                "idleTimeout", hikariDataSource.getIdleTimeout(),
                "maxLifetime", hikariDataSource.getMaxLifetime(),
                "leakDetectionThreshold", hikariDataSource.getLeakDetectionThreshold()
        );
    }
}
