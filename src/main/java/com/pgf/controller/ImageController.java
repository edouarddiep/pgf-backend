package com.pgf.controller;

import com.pgf.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "Locally stored image delivery")
@Slf4j
public class ImageController {

    private final Path uploadRoot;

    public ImageController(@Value("${app.upload.dir:${user.home}/pgf-uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping("/{category}/{filename}")
    @Operation(summary = "Serve a stored image")
    public ResponseEntity<Resource> getImage(@PathVariable String category, @PathVariable String filename) throws IOException {
        Path filePath = resolveWithinRoot(category, filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new EntityNotFoundException("Image not found: " + category + "/" + filename);
        }

        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    private Path resolveWithinRoot(String category, String filename) {
        Path filePath = uploadRoot.resolve(category).resolve(filename).normalize();
        if (!filePath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid image path: " + category + "/" + filename);
        }
        return filePath;
    }
}
