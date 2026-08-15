package com.pgf.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgf.dto.ExhibitionDto;
import com.pgf.service.AuditLogService;
import com.pgf.service.ExhibitionService;
import com.pgf.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/exhibitions")
@Tag(name = "Admin - Exhibitions", description = "Exhibition administration")
@RequiredArgsConstructor
public class AdminExhibitionController {

    private static final String ENTITY_TYPE = "exhibition";
    private static final String IMAGE_FOLDER = "exhibitions";

    private final ExhibitionService exhibitionService;
    private final FileUploadService fileUploadService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

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
        return auditedCreate(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update exhibition")
    public ExhibitionDto update(@PathVariable Long id, @Valid @RequestBody ExhibitionDto dto) {
        return auditedUpdate(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete exhibition")
    public void delete(@PathVariable Long id) {
        ExhibitionDto before = exhibitionService.findById(id);
        exhibitionService.delete(id);
        auditLogService.logDelete(ENTITY_TYPE, id, before);
    }

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create exhibition together with its images")
    public ExhibitionDto createWithImages(@RequestPart("exhibition") String exhibitionJson,
                                          @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        ExhibitionDto dto = objectMapper.readValue(exhibitionJson, ExhibitionDto.class);
        List<String> uploaded = fileUploadService.uploadImages(images, IMAGE_FOLDER);

        if (!uploaded.isEmpty()) {
            dto.setImageUrls(uploaded);
            dto.setImageUrl(uploaded.get(0));
        }
        return auditedCreate(dto);
    }

    @PutMapping(value = "/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update exhibition together with additional images")
    public ExhibitionDto updateWithImages(@PathVariable Long id,
                                          @RequestPart("exhibition") String exhibitionJson,
                                          @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        ExhibitionDto dto = objectMapper.readValue(exhibitionJson, ExhibitionDto.class);
        List<String> uploaded = fileUploadService.uploadImages(images, IMAGE_FOLDER);

        if (!uploaded.isEmpty()) {
            List<String> imageUrls = new ArrayList<>(dto.getImageUrls() == null ? List.of() : dto.getImageUrls());
            imageUrls.addAll(uploaded);
            dto.setImageUrls(imageUrls);

            if (dto.getImageUrl() == null) {
                dto.setImageUrl(uploaded.get(0));
            }
        }
        return auditedUpdate(id, dto);
    }

    private ExhibitionDto auditedCreate(ExhibitionDto dto) {
        ExhibitionDto created = exhibitionService.create(dto);
        auditLogService.logCreate(ENTITY_TYPE, created.getId(), created);
        return created;
    }

    private ExhibitionDto auditedUpdate(Long id, ExhibitionDto dto) {
        ExhibitionDto before = exhibitionService.findById(id);
        ExhibitionDto updated = exhibitionService.update(id, dto);
        auditLogService.logUpdate(ENTITY_TYPE, id, before, updated);
        return updated;
    }
}
