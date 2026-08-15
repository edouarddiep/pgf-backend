package com.pgf.controller.admin;

import com.pgf.dto.ArchiveDto;
import com.pgf.service.ArchiveService;
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
@RequestMapping("/api/admin/archives")
@Tag(name = "Admin - Archives", description = "Archive administration")
@RequiredArgsConstructor
public class AdminArchiveController {

    private static final String ENTITY_TYPE = "archive";

    private final ArchiveService archiveService;
    private final AuditLogService auditLogService;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create archive")
    public ArchiveDto create(@Valid @RequestBody ArchiveDto dto) {
        ArchiveDto created = archiveService.create(dto);
        auditLogService.logCreate(ENTITY_TYPE, created.getId(), created);
        return created;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update archive")
    public ArchiveDto update(@PathVariable Long id, @Valid @RequestBody ArchiveDto dto) {
        ArchiveDto before = archiveService.findById(id);
        ArchiveDto updated = archiveService.update(id, dto);
        auditLogService.logUpdate(ENTITY_TYPE, id, before, updated);
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete archive")
    public void delete(@PathVariable Long id) {
        ArchiveDto before = archiveService.findById(id);
        archiveService.delete(id);
        auditLogService.logDelete(ENTITY_TYPE, id, before);
    }
}
