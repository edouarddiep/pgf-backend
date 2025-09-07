package com.pgf.controller;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.service.ArtworkCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@Tag(name = "Categories", description = "Artwork category management endpoints")
@RequiredArgsConstructor
public class ArtworkCategoryController {

    private final ArtworkCategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all artwork categories")
    public ResponseEntity<List<ArtworkCategoryDto>> getAllCategories() {
        List<ArtworkCategoryDto> categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ArtworkCategoryDto> getCategoryById(@PathVariable Long id) {
        ArtworkCategoryDto category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public ResponseEntity<ArtworkCategoryDto> getCategoryBySlug(@PathVariable String slug) {
        ArtworkCategoryDto category = categoryService.findBySlug(slug);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @Operation(summary = "Create new category")
    public ResponseEntity<ArtworkCategoryDto> createCategory(@Valid @RequestBody ArtworkCategoryDto categoryDto) {
        ArtworkCategoryDto createdCategory = categoryService.create(categoryDto);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ArtworkCategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody ArtworkCategoryDto categoryDto) {
        ArtworkCategoryDto updatedCategory = categoryService.update(id, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}