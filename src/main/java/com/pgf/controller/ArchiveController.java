package com.pgf.controller;

import com.pgf.dto.ArchiveDto;
import com.pgf.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/archives")
@Tag(name = "Archives", description = "Public archive catalogue")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @GetMapping
    @Operation(summary = "Get all archives")
    public List<ArchiveDto> findAll() {
        return archiveService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get archive by ID")
    public ArchiveDto findById(@PathVariable Long id) {
        return archiveService.findById(id);
    }
}
