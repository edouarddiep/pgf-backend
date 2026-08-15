package com.pgf.service;

import com.pgf.config.CacheConfig;
import com.pgf.repository.ArtworkCategoryRepository;
import com.pgf.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SitemapService {

    private static final List<String> LOCALES = List.of("fr-ch", "en-ch");
    private static final List<StaticPage> STATIC_PAGES = List.of(
            new StaticPage("about", "0.8", "monthly"),
            new StaticPage("artworks", "0.9", "weekly"),
            new StaticPage("exhibitions", "0.9", "weekly"),
            new StaticPage("archives", "0.7", "monthly"),
            new StaticPage("association", "0.6", "monthly"),
            new StaticPage("contact", "0.6", "monthly")
    );

    private final ArtworkCategoryRepository categoryRepository;
    private final ArtworkRepository artworkRepository;

    @Value("${app.public-url:https://www.pierrette-gonsethfavre.ch}")
    private String baseUrl;

    @Cacheable(CacheConfig.SITEMAP)
    @Transactional(readOnly = true)
    public String generate() {
        StringBuilder xml = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        appendUrl(xml, "", "1.0", "weekly");
        STATIC_PAGES.forEach(page -> appendLocalized(xml, "/" + page.path(), page.priority(), page.changeFrequency()));

        categoryRepository.findAllSlugs()
                .forEach(slug -> appendLocalized(xml, "/artworks/" + slug, "0.8", "weekly"));

        artworkRepository.findSitemapEntries()
                .forEach(artwork -> appendLocalized(xml,
                        "/artworks/" + artwork.getCategorySlug() + "/" + artwork.getId(), "0.7", "monthly"));

        return xml.append("</urlset>").toString();
    }

    private void appendLocalized(StringBuilder xml, String path, String priority, String changeFrequency) {
        LOCALES.forEach(locale -> appendUrl(xml, "/" + locale + path, priority, changeFrequency));
    }

    private void appendUrl(StringBuilder xml, String path, String priority, String changeFrequency) {
        xml.append("<url>")
                .append("<loc>").append(baseUrl).append(path).append("</loc>")
                .append("<changefreq>").append(changeFrequency).append("</changefreq>")
                .append("<priority>").append(priority).append("</priority>")
                .append("</url>");
    }

    private record StaticPage(String path, String priority, String changeFrequency) {}
}
