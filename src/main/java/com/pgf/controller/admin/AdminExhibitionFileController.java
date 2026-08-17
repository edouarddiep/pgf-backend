package com.pgf.controller.admin;

import com.pgf.dto.ExhibitionFileDto;
import com.pgf.model.ExhibitionFile;
import com.pgf.service.AuditLogService;
import com.pgf.service.ExhibitionFileService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exhibitions/{exhibitionId}/files")
@Tag(name = "Admin - Exhibition files", description = "Exhibition media administration")
@RequiredArgsConstructor
public class AdminExhibitionFileController {

    private static final String ENTITY_TYPE = "exhibitionFile";

    private final ExhibitionFileService exhibitionFileService;
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get all files of an exhibition")
    public List<ExhibitionFileDto> findAll(@PathVariable Long exhibitionId,
                                           @RequestParam(required = false) ExhibitionFile.MediaType mediaType) {
        return exhibitionFileService.findAll(exhibitionId, mediaType);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Attach a file to an exhibition")
    public ExhibitionFileDto create(@PathVariable Long exhibitionId, @Valid @RequestBody ExhibitionFileDto dto) {
        ExhibitionFileDto created = exhibitionFileService.create(exhibitionId, dto);
        auditLogService.logCreate(ENTITY_TYPE, created.getId(), created);
        return created;
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Attach several files to an exhibition")
    public List<ExhibitionFileDto> createAll(@PathVariable Long exhibitionId, @Valid @RequestBody List<ExhibitionFileDto> dtos) {
        List<ExhibitionFileDto> created = exhibitionFileService.createAll(exhibitionId, dtos);
        created.forEach(file -> auditLogService.logCreate(ENTITY_TYPE, file.getId(), file));
        return created;
    }

    @PutMapping("/{fileId}")
    @Operation(summary = "Update an exhibition file")
    public ExhibitionFileDto update(@PathVariable Long exhibitionId,
                                    @PathVariable Long fileId,
                                    @Valid @RequestBody ExhibitionFileDto dto) {
        ExhibitionFileDto before = exhibitionFileService.findById(exhibitionId, fileId);
        ExhibitionFileDto updated = exhibitionFileService.update(exhibitionId, fileId, dto);
        auditLogService.logUpdate(ENTITY_TYPE, fileId, before, updated);
        return updated;
    }

    @PutMapping("/order")
    @Operation(summary = "Reorder the files of an exhibition")
    public List<ExhibitionFileDto> reorder(@PathVariable Long exhibitionId, @RequestBody List<Long> orderedFileIds) {
        return exhibitionFileService.reorder(exhibitionId, orderedFileIds);
    }

    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an exhibition file")
    public void delete(@PathVariable Long exhibitionId, @PathVariable Long fileId) {
        ExhibitionFileDto before = exhibitionFileService.findById(exhibitionId, fileId);
        exhibitionFileService.delete(exhibitionId, fileId);
        auditLogService.logDelete(ENTITY_TYPE, fileId, before);
    }
}
