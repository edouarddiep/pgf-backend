package com.pgf.controller.admin;

import com.pgf.dto.ExhibitionDto;
import com.pgf.service.AuditLogService;
import com.pgf.service.ExhibitionService;
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
@RequestMapping("/api/admin/exhibitions")
@Tag(name = "Admin - Exhibitions", description = "Exhibition administration")
@RequiredArgsConstructor
public class AdminExhibitionController {

    private static final String ENTITY_TYPE = "exhibition";

    private final ExhibitionService exhibitionService;
    private final AuditLogService auditLogService;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create exhibition")
    public ExhibitionDto create(@Valid @RequestBody ExhibitionDto dto) {
        ExhibitionDto created = exhibitionService.create(dto);
        auditLogService.logCreate(ENTITY_TYPE, created.getId(), created);
        return created;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update exhibition")
    public ExhibitionDto update(@PathVariable Long id, @Valid @RequestBody ExhibitionDto dto) {
        ExhibitionDto before = exhibitionService.findById(id);
        ExhibitionDto updated = exhibitionService.update(id, dto);
        auditLogService.logUpdate(ENTITY_TYPE, id, before, updated);
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete exhibition")
    public void delete(@PathVariable Long id) {
        ExhibitionDto before = exhibitionService.findById(id);
        exhibitionService.delete(id);
        auditLogService.logDelete(ENTITY_TYPE, id, before);
    }
}
