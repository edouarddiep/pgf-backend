package com.pgf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.time.Duration;

@Configuration
public class WebCacheConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor interceptor = new WebContentInterceptor();

        // Contenu éditable depuis l'admin : une publication doit se voir en ligne
        // sans délai notable. La fenêtre reste assez large pour absorber les
        // rechargements successifs d'un même visiteur, et l'ETag rend la
        // revalidation quasi gratuite une fois ce court délai passé.
        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofSeconds(60))
                        .cachePublic()
                        .staleWhileRevalidate(Duration.ofSeconds(60)),
                "/api/artworks/**", "/api/categories/**", "/api/exhibitions/**", "/api/archives/**");

        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofHours(6)).cachePublic(), "/sitemap.xml");
        interceptor.addCacheMapping(CacheControl.noStore(), "/api/admin/**", "/api/contact/**");

        registry.addInterceptor(interceptor);
    }

    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }
}
