package com.pgf.controller;

import com.pgf.repository.ArtworkCategoryRepository;
import com.pgf.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SitemapController {

    private final ArtworkCategoryRepository categoryRepository;
    private final ArtworkRepository artworkRepository;

    private static final String BASE_URL = "https://www.pierrette-gonsethfavre.ch";

    @Cacheable("sitemap")
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        addUrl(xml, "", "1.0", "weekly");
        addUrl(xml, "/fr-ch/about", "0.8", "monthly");
        addUrl(xml, "/en-ch/about", "0.8", "monthly");
        addUrl(xml, "/fr-ch/artworks", "0.9", "weekly");
        addUrl(xml, "/en-ch/artworks", "0.9", "weekly");
        addUrl(xml, "/fr-ch/exhibitions", "0.9", "weekly");
        addUrl(xml, "/en-ch/exhibitions", "0.9", "weekly");
        addUrl(xml, "/fr-ch/archives", "0.7", "monthly");
        addUrl(xml, "/en-ch/archives", "0.7", "monthly");
        addUrl(xml, "/fr-ch/association", "0.6", "monthly");
        addUrl(xml, "/en-ch/association", "0.6", "monthly");
        addUrl(xml, "/fr-ch/contact", "0.6", "monthly");
        addUrl(xml, "/en-ch/contact", "0.6", "monthly");

        categoryRepository.findAll().forEach(cat -> {
            addUrl(xml, "/fr-ch/artworks/" + cat.getSlug(), "0.8", "weekly");
            addUrl(xml, "/en-ch/artworks/" + cat.getSlug(), "0.8", "weekly");
        });

        artworkRepository.findAll().forEach(artwork ->
                artwork.getCategories().stream().findFirst().ifPresent(cat -> {
                    addUrl(xml, "/fr-ch/artworks/" + cat.getSlug() + "/" + artwork.getId(), "0.7", "monthly");
                    addUrl(xml, "/en-ch/artworks/" + cat.getSlug() + "/" + artwork.getId(), "0.7", "monthly");
                })
        );

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    private void addUrl(StringBuilder xml, String path, String priority, String changefreq) {
        xml.append("<url>")
                .append("<loc>").append(BASE_URL).append(path).append("</loc>")
                .append("<changefreq>").append(changefreq).append("</changefreq>")
                .append("<priority>").append(priority).append("</priority>")
                .append("</url>");
    }
}