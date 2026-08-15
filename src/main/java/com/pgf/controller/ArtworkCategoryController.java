package com.pgf.controller;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.service.ArtworkCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Public artwork categories")
@RequiredArgsConstructor
public class ArtworkCategoryController {

    private final ArtworkCategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all artwork categories")
    public List<ArtworkCategoryDto> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ArtworkCategoryDto findById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public ArtworkCategoryDto findBySlug(@PathVariable String slug) {
        return categoryService.findBySlug(slug);
    }
}
