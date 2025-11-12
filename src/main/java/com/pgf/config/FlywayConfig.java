package com.pgf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Value("${flyway.repair-on-startup:false}")
    private boolean repairOnStartup;

    @Bean
    public FlywayMigrationStrategy repairStrategy() {
        return flyway -> {
            if (repairOnStartup) {
                flyway.repair();
            }
            flyway.migrate();
        };
    }
}
