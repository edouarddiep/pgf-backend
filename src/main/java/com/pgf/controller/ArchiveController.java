package com.pgf.controller;

import com.pgf.dto.ArchiveDto;
import com.pgf.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archives")
@Tag(name = "Archives", description = "Archive management endpoints")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @GetMapping
    @Operation(summary = "Get all archives")
    public ResponseEntity<List<ArchiveDto>> getAllArchives() {
        return ResponseEntity.ok(archiveService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get archive by ID")
    public ResponseEntity<ArchiveDto> getArchiveById(@PathVariable Long id) {
        return archiveService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new archive")
    public ResponseEntity<ArchiveDto> createArchive(@Valid @RequestBody ArchiveDto archiveDto) {
        ArchiveDto createdArchive = archiveService.create(archiveDto);
        return new ResponseEntity<>(createdArchive, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update archive")
    public ResponseEntity<ArchiveDto> updateArchive(@PathVariable Long id, @Valid @RequestBody ArchiveDto archiveDto) {
        ArchiveDto updatedArchive = archiveService.update(id, archiveDto);
        return ResponseEntity.ok(updatedArchive);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete archive")
    public ResponseEntity<Void> deleteArchive(@PathVariable Long id) {
        archiveService.delete(id);
        return ResponseEntity.noContent().build();
    }
}