package com.pgf.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ConnectionPoolHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public ConnectionPoolHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            return Health.unknown().withDetail("dataSource", "Not HikariCP").build();
        }

        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
        int threadsAwaiting = pool.getThreadsAwaitingConnection();
        int activeConnections = pool.getActiveConnections();
        int maxPoolSize = hikari.getMaximumPoolSize();
        double usagePercent = (activeConnections * 100.0) / maxPoolSize;

        Health.Builder builder;

        if (threadsAwaiting > 0) {
            builder = Health.down()
                    .withDetail("issue", "Threads waiting for connections - Pool saturated");
        } else if (usagePercent > 90) {
            builder = Health.down()
                    .withDetail("warning", "Pool usage very high - Risk of saturation");
        } else if (usagePercent > 70) {
            builder = Health.up()
                    .withDetail("warning", "Pool usage high");
        } else {
            builder = Health.up();
        }

        return builder
                .withDetail("activeConnections", activeConnections)
                .withDetail("idleConnections", pool.getIdleConnections())
                .withDetail("totalConnections", pool.getTotalConnections())
                .withDetail("maxPoolSize", maxPoolSize)
                .withDetail("threadsAwaiting", threadsAwaiting)
                .withDetail("poolUsagePercent", String.format("%.1f%%", usagePercent))
                .withDetail("poolMode", hikari.getJdbcUrl().contains(":6543")
                        ? "Transaction (6543)" : "Session (5432)")
                .build();
    }
}