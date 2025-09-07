package com.pgf.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI pgfOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PGF Artist Website API")
                        .description("RESTful API for PGF artist portfolio website")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PGF")
                                .email("contact@pgf-artist.com")));
    }
}