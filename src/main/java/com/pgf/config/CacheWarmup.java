package com.pgf.config;

import com.pgf.service.ArchiveService;
import com.pgf.service.ArtworkCategoryService;
import com.pgf.service.ArtworkService;
import com.pgf.service.ExhibitionService;
import com.pgf.service.SitemapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CacheWarmup {

    private final ArtworkCategoryService categoryService;
    private final ArtworkService artworkService;
    private final ExhibitionService exhibitionService;
    private final ArchiveService archiveService;
    private final SitemapService sitemapService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {
        try {
            categoryService.findAll();
            artworkService.findAll();
            exhibitionService.findAll();
            archiveService.findAll();
            sitemapService.generate();
            log.info("Caches warmed up");
        } catch (Exception e) {
            log.warn("Cache warmup skipped: {}", e.getMessage());
        }
    }
}
