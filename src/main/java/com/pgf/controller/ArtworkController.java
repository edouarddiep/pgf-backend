package com.pgf.controller;

import com.pgf.dto.ArtworkDto;
import com.pgf.service.ArtworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/artworks")
@CrossOrigin(origins = "*")
@Tag(name = "Artworks", description = "Artwork management endpoints")
@RequiredArgsConstructor
public class ArtworkController {

    private final ArtworkService artworkService;

    @GetMapping
    @Operation(summary = "Get all artworks")
    public ResponseEntity<List<ArtworkDto>> getAllArtworks() {
        List<ArtworkDto> artworks = artworkService.findAll();
        return ResponseEntity.ok(artworks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get artwork by ID")
    public ResponseEntity<ArtworkDto> getArtworkById(@PathVariable Long id) {
        ArtworkDto artwork = artworkService.findById(id);
        return ResponseEntity.ok(artwork);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get artworks by category ID")
    public ResponseEntity<List<ArtworkDto>> getArtworksByCategory(@PathVariable Long categoryId) {
        List<ArtworkDto> artworks = artworkService.findByCategoryId(categoryId);
        return ResponseEntity.ok(artworks);
    }

    @GetMapping("/category/slug/{categorySlug}")
    @Operation(summary = "Get artworks by category slug")
    public ResponseEntity<List<ArtworkDto>> getArtworksByCategorySlug(@PathVariable String categorySlug) {
        List<ArtworkDto> artworks = artworkService.findByCategorySlug(categorySlug);
        return ResponseEntity.ok(artworks);
    }

    @PostMapping
    @Operation(summary = "Create new artwork")
    public ResponseEntity<ArtworkDto> createArtwork(@Valid @RequestBody ArtworkDto artworkDto) {
        ArtworkDto createdArtwork = artworkService.create(artworkDto);
        return new ResponseEntity<>(createdArtwork, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update artwork")
    public ResponseEntity<ArtworkDto> updateArtwork(@PathVariable Long id, @Valid @RequestBody ArtworkDto artworkDto) {
        ArtworkDto updatedArtwork = artworkService.update(id, artworkDto);
        return ResponseEntity.ok(updatedArtwork);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete artwork")
    public ResponseEntity<Void> deleteArtwork(@PathVariable Long id) {
        artworkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}