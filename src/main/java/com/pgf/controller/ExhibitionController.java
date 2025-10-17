package com.pgf.controller;

import com.pgf.dto.ExhibitionDto;
import com.pgf.service.ExhibitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@Tag(name = "Exhibitions", description = "Exhibition management endpoints")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    @Operation(summary = "Get all exhibitions")
    public ResponseEntity<List<ExhibitionDto>> getAllExhibitions() {
        return ResponseEntity.ok(exhibitionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exhibition by ID")
    public ResponseEntity<ExhibitionDto> getExhibitionById(@PathVariable Long id) {
        return exhibitionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming exhibitions")
    public ResponseEntity<List<ExhibitionDto>> getUpcomingExhibitions() {
        return ResponseEntity.ok(exhibitionService.findUpcomingExhibitions());
    }

    @GetMapping("/ongoing")
    @Operation(summary = "Get ongoing exhibitions")
    public ResponseEntity<List<ExhibitionDto>> getOngoingExhibitions() {
        return ResponseEntity.ok(exhibitionService.findOngoingExhibitions());
    }

    @GetMapping("/past")
    @Operation(summary = "Get past exhibitions")
    public ResponseEntity<List<ExhibitionDto>> getPastExhibitions() {
        return ResponseEntity.ok(exhibitionService.findPastExhibitions());
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

    @PutMapping("/{id}/order")
    @Operation(summary = "Update exhibition display order")
    public ResponseEntity<Void> updateExhibitionOrder(@PathVariable Long id, @RequestParam Integer displayOrder) {
        exhibitionService.updateDisplayOrder(id, displayOrder);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete exhibition")
    public ResponseEntity<Void> deleteExhibition(@PathVariable Long id) {
        exhibitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}