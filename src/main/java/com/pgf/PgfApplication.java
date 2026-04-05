package com.pgf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PgfApplication {

    public static void main(String[] args) {
        SpringApplication.run(PgfApplication.class, args);
    }
}