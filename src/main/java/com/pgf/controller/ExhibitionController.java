package com.pgf.controller;

import com.pgf.dto.ExhibitionDto;
import com.pgf.service.ExhibitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@Tag(name = "Exhibitions", description = "Public exhibition programme")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    @Operation(summary = "Get all exhibitions")
    public List<ExhibitionDto> findAll() {
        return exhibitionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exhibition by ID")
    public ExhibitionDto findById(@PathVariable Long id) {
        return exhibitionService.findById(id);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming exhibitions")
    public List<ExhibitionDto> findUpcoming() {
        return exhibitionService.findUpcoming();
    }

    @GetMapping("/ongoing")
    @Operation(summary = "Get ongoing exhibitions")
    public List<ExhibitionDto> findOngoing() {
        return exhibitionService.findOngoing();
    }

    @GetMapping("/past")
    @Operation(summary = "Get past exhibitions")
    public List<ExhibitionDto> findPast() {
        return exhibitionService.findPast();
    }
}
