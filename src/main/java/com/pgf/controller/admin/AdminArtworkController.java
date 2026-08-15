package com.pgf.controller.admin;

import com.pgf.dto.ArtworkDto;
import com.pgf.service.ArtworkService;
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
@RequestMapping("/api/admin/artworks")
@Tag(name = "Admin - Artworks", description = "Artwork administration")
@RequiredArgsConstructor
public class AdminArtworkController {

    private static final String ENTITY_TYPE = "artwork";

    private final ArtworkService artworkService;
    private final AuditLogService auditLogService;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create artwork")
    public ArtworkDto create(@Valid @RequestBody ArtworkDto dto) {
        ArtworkDto created = artworkService.create(dto);
        auditLogService.logCreate(ENTITY_TYPE, created.getId(), created);
        return created;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update artwork")
    public ArtworkDto update(@PathVariable Long id, @Valid @RequestBody ArtworkDto dto) {
        ArtworkDto before = artworkService.findById(id);
        ArtworkDto updated = artworkService.update(id, dto);
        auditLogService.logUpdate(ENTITY_TYPE, id, before, updated);
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete artwork")
    public void delete(@PathVariable Long id) {
        ArtworkDto before = artworkService.findById(id);
        artworkService.delete(id);
        auditLogService.logDelete(ENTITY_TYPE, id, before);
    }
}
