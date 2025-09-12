package com.pgf.controller;

import com.pgf.dto.ExhibitionDto;
import com.pgf.service.ExhibitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exhibitions")
@CrossOrigin(origins = "*")
@Tag(name = "Exhibitions", description = "Exhibition management endpoints")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    @Operation(summary = "Get all exhibitions")
    public ResponseEntity<List<ExhibitionDto>> getAllExhibitions() {
        List<ExhibitionDto> exhibitions = exhibitionService.findAll();
        return ResponseEntity.ok(exhibitions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exhibition by ID")
    public ResponseEntity<ExhibitionDto> getExhibitionById(@PathVariable Long id) {
        Optional<ExhibitionDto> exhibition = exhibitionService.findById(id);
        return exhibition.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming exhibitions")
    public ResponseEntity<List<ExhibitionDto>> getUpcomingExhibitions() {
        List<ExhibitionDto> exhibitions = exhibitionService.findUpcomingExhibitions();
        return ResponseEntity.ok(exhibitions);
    }

    @GetMapping("/past")
    @Operation(summary = "Get past exhibitions")
    public ResponseEntity<List<ExhibitionDto>> getPastExhibitions() {
        List<ExhibitionDto> exhibitions = exhibitionService.findPastExhibitions();
        return ResponseEntity.ok(exhibitions);
    }

    @GetMapping("/ongoing")
    @Operation(summary = "Get ongoing exhibitions")
    public ResponseEntity<List<ExhibitionDto>> getOngoingExhibitions() {
        List<ExhibitionDto> exhibitions = exhibitionService.findOngoingExhibitions();
        return ResponseEntity.ok(exhibitions);
    }

    @GetMapping("/next-featured")
    @Operation(summary = "Get next featured exhibition")
    public ResponseEntity<ExhibitionDto> getNextFeaturedExhibition() {
        Optional<ExhibitionDto> exhibition = exhibitionService.findFeaturedExhibition();
        return exhibition.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new exhibition")
    public ResponseEntity<ExhibitionDto> createExhibition(@Valid @RequestBody ExhibitionDto exhibitionDto) {
        ExhibitionDto createdExhibition = exhibitionService.create(exhibitionDto);
        return new ResponseEntity<>(createdExhibition, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update exhibition")
    public ResponseEntity<ExhibitionDto> updateExhibition(@PathVariable Long id, @Valid @RequestBody ExhibitionDto exhibitionDto) {
        ExhibitionDto updatedExhibition = exhibitionService.update(id, exhibitionDto);
        return ResponseEntity.ok(updatedExhibition);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete exhibition")
    public ResponseEntity<Void> deleteExhibition(@PathVariable Long id) {
        exhibitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}