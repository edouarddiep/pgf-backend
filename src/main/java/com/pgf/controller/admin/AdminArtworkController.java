package com.pgf.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgf.dto.ArtworkDto;
import com.pgf.service.ArtworkCategoryService;
import com.pgf.service.ArtworkService;
import com.pgf.service.AuditLogService;
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
import java.util.Set;

@RestController
@RequestMapping("/api/admin/artworks")
@Tag(name = "Admin - Artworks", description = "Artwork administration")
@RequiredArgsConstructor
public class AdminArtworkController {

    private static final String ENTITY_TYPE = "artwork";

    private final ArtworkService artworkService;
    private final ArtworkCategoryService categoryService;
    private final FileUploadService fileUploadService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "Get all artworks")
    public List<ArtworkDto> findAll() {
        return artworkService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get artwork by ID")
    public ArtworkDto findById(@PathVariable Long id) {
        return artworkService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create artwork")
    public ArtworkDto create(@Valid @RequestBody ArtworkDto dto) {
        return auditedCreate(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update artwork")
    public ArtworkDto update(@PathVariable Long id, @Valid @RequestBody ArtworkDto dto) {
        return auditedUpdate(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete artwork")
    public void delete(@PathVariable Long id) {
        ArtworkDto before = artworkService.findById(id);
        artworkService.delete(id);
        auditLogService.logDelete(ENTITY_TYPE, id, before);
    }

    @PutMapping("/{id}/categories")
    @Operation(summary = "Replace the categories of an artwork")
    public ArtworkDto updateCategories(@PathVariable Long id, @RequestBody Set<Long> categoryIds) {
        return artworkService.updateCategories(id, categoryIds);
    }

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create artwork together with its images")
    public ArtworkDto createWithImages(@RequestPart("artwork") String artworkJson,
                                       @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        ArtworkDto dto = objectMapper.readValue(artworkJson, ArtworkDto.class);
        List<String> uploaded = uploadImages(dto, images);

        if (!uploaded.isEmpty()) {
            dto.setImageUrls(uploaded);
            dto.setMainImageUrl(uploaded.get(0));
        }
        return auditedCreate(dto);
    }

    @PutMapping(value = "/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update artwork together with additional images")
    public ArtworkDto updateWithImages(@PathVariable Long id,
                                       @RequestPart("artwork") String artworkJson,
                                       @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        ArtworkDto dto = objectMapper.readValue(artworkJson, ArtworkDto.class);
        List<String> uploaded = uploadImages(dto, images);

        if (!uploaded.isEmpty()) {
            List<String> imageUrls = new ArrayList<>(dto.getImageUrls() == null ? List.of() : dto.getImageUrls());
            imageUrls.addAll(uploaded);
            dto.setImageUrls(imageUrls);

            if (dto.getMainImageUrl() == null) {
                dto.setMainImageUrl(uploaded.get(0));
            }
        }
        return auditedUpdate(id, dto);
    }

    private ArtworkDto auditedCreate(ArtworkDto dto) {
        ArtworkDto created = artworkService.create(dto);
        auditLogService.logCreate(ENTITY_TYPE, created.getId(), created);
        return created;
    }

    private ArtworkDto auditedUpdate(Long id, ArtworkDto dto) {
        ArtworkDto before = artworkService.findById(id);
        ArtworkDto updated = artworkService.update(id, dto);
        auditLogService.logUpdate(ENTITY_TYPE, id, before, updated);
        return updated;
    }

    private List<String> uploadImages(ArtworkDto dto, List<MultipartFile> images) throws IOException {
        return fileUploadService.uploadImages(images, categoryService.resolveSlug(dto.getCategoryIds()));
    }
}
