package com.pgf.controller.admin;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.service.ArtworkCategoryService;
import com.pgf.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = "Admin - Categories", description = "Artwork category administration")
@RequiredArgsConstructor
public class AdminCategoryController {

    private static final String ENTITY_TYPE = "artwork_category";

    private final ArtworkCategoryService categoryService;
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public List<ArtworkCategoryDto> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ArtworkCategoryDto findById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create category")
    public ArtworkCategoryDto create(@Valid @RequestBody ArtworkCategoryDto dto) {
        ArtworkCategoryDto created = categoryService.create(dto);
        auditLogService.logCreate(ENTITY_TYPE, created.getId(), created);
        return created;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public ArtworkCategoryDto update(@PathVariable Long id, @Valid @RequestBody ArtworkCategoryDto dto) {
        ArtworkCategoryDto before = categoryService.findById(id);
        ArtworkCategoryDto updated = categoryService.update(id, dto);
        auditLogService.logUpdate(ENTITY_TYPE, id, before, updated);
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category")
    public void delete(@PathVariable Long id) {
        ArtworkCategoryDto before = categoryService.findById(id);
        categoryService.delete(id);
        auditLogService.logDelete(ENTITY_TYPE, id, before);
    }
}
