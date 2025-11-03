package com.pgf.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
public class ConnectionPoolMonitor {

    private final DataSource dataSource;

    public ConnectionPoolMonitor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Scheduled(fixedRate = 60000)
    public void logPoolStats() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();

            log.info("=== HikariCP Pool Stats ===");
            log.info("Active connections: {}", poolMXBean.getActiveConnections());
            log.info("Idle connections: {}", poolMXBean.getIdleConnections());
            log.info("Total connections: {}", poolMXBean.getTotalConnections());
            log.info("Threads awaiting connection: {}", poolMXBean.getThreadsAwaitingConnection());
            log.info("Max pool size: {}", hikariDataSource.getMaximumPoolSize());

            int activeConnections = poolMXBean.getActiveConnections();
            int maxPoolSize = hikariDataSource.getMaximumPoolSize();
            double usagePercent = (activeConnections * 100.0) / maxPoolSize;

            if (usagePercent > 80) {
                log.warn("⚠️ Pool usage is high: {}%", String.format("%.1f", usagePercent));
            } else {
                log.info("✓ Pool usage: {}%", String.format("%.1f", usagePercent));
            }
        }
    }
}