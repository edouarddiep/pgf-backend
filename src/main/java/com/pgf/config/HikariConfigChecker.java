package com.pgf.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class HikariConfigChecker {

    private static final Logger log = LoggerFactory.getLogger(HikariConfigChecker.class);

    @Bean
    public CommandLineRunner logHikariConfig(DataSource dataSource) {
        return args -> {
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                log.info("=== Configuration HikariCP ===");
                log.info("Maximum Pool Size: {}", hikariDataSource.getMaximumPoolSize());
                log.info("Minimum Idle: {}", hikariDataSource.getMinimumIdle());
                log.info("Connection Timeout: {} ms", hikariDataSource.getConnectionTimeout());
                log.info("Idle Timeout: {} ms", hikariDataSource.getIdleTimeout());
                log.info("Max Lifetime: {} ms", hikariDataSource.getMaxLifetime());
                log.info("Leak Detection Threshold: {} ms", hikariDataSource.getLeakDetectionThreshold());
                log.info("Pool Name: {}", hikariDataSource.getPoolName());
                log.info("JDBC URL: {}", hikariDataSource.getJdbcUrl());
                log.info("==============================");
            }
        };
    }
}