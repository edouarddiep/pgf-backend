package com.pgf.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class DeployHookConfig implements WebMvcConfigurer {

    private final DeployHookInterceptor deployHookInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(deployHookInterceptor)
                .addPathPatterns("/api/admin/**")
                // Connexion, messages et téléversements ne changent rien au HTML publié :
                // seul l'enregistrement de l'entité qui référence le fichier le fait.
                .excludePathPatterns(
                        "/api/admin/auth/**",
                        "/api/admin/messages/**",
                        "/api/admin/upload/**");
    }
}
