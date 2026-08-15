package com.pgf.controller;

import com.pgf.dto.ArtworkDto;
import com.pgf.service.ArtworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/artworks")
@Tag(name = "Artworks", description = "Public artwork catalogue")
@RequiredArgsConstructor
public class ArtworkController {

    private final ArtworkService artworkService;

    @GetMapping
    @Operation(summary = "Get all artworks")
    public List<ArtworkDto> findAll() {
        return artworkService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get artwork by ID")
    public ArtworkDto findById(@PathVariable Long id) {
        return artworkService.findById(id);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get artworks by category ID")
    public List<ArtworkDto> findByCategoryId(@PathVariable Long categoryId) {
        return artworkService.findByCategoryId(categoryId);
    }

    @GetMapping("/category/slug/{categorySlug}")
    @Operation(summary = "Get artworks by category slug")
    public List<ArtworkDto> findByCategorySlug(@PathVariable String categorySlug) {
        return artworkService.findByCategorySlug(categorySlug);
    }
}
