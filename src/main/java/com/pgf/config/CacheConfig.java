package com.pgf.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CacheConfig {

    public static final String ARTWORKS = "artworks";
    public static final String ARTWORK = "artwork";
    public static final String ARTWORKS_BY_CATEGORY = "artworksByCategory";
    public static final String CATEGORIES = "categories";
    public static final String CATEGORY = "category";
    public static final String EXHIBITIONS = "exhibitions";
    public static final String EXHIBITIONS_BY_STATUS = "exhibitionsByStatus";
    public static final String ARCHIVES = "archives";
    public static final String ARCHIVE = "archive";
    public static final String SITEMAP = "sitemap";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                ARTWORKS, ARTWORK, ARTWORKS_BY_CATEGORY,
                CATEGORIES, CATEGORY,
                EXHIBITIONS, ARCHIVES, ARCHIVE);

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofHours(1))
                .recordStats());

        cacheManager.registerCustomCache(SITEMAP, Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(Duration.ofHours(6))
                .recordStats()
                .build());

        cacheManager.registerCustomCache(EXHIBITIONS_BY_STATUS, Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats()
                .build());

        return cacheManager;
    }
}
