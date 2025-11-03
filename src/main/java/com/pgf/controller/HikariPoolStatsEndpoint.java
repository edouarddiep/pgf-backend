package com.pgf.controller;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/hikari")
public class HikariPoolStatsEndpoint {

    private final DataSource dataSource;

    public HikariPoolStatsEndpoint(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getHikariStats() {
        Map<String, Object> stats = new HashMap<>();

        if (dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();

            stats.put("activeConnections", poolMXBean.getActiveConnections());
            stats.put("idleConnections", poolMXBean.getIdleConnections());
            stats.put("totalConnections", poolMXBean.getTotalConnections());
            stats.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());

            stats.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
            stats.put("minimumIdle", hikariDataSource.getMinimumIdle());
            stats.put("connectionTimeout", hikariDataSource.getConnectionTimeout());
            stats.put("idleTimeout", hikariDataSource.getIdleTimeout());
            stats.put("maxLifetime", hikariDataSource.getMaxLifetime());
            stats.put("leakDetectionThreshold", hikariDataSource.getLeakDetectionThreshold());
        }

        return ResponseEntity.ok(stats);
    }
}