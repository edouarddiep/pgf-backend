package com.pgf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pgf.dto.*;
import com.pgf.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Admin", description = "Admin management endpoints")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final ImageService imageService;

    @PostMapping("/auth/login")
    @Operation(summary = "Admin login")
    public ResponseEntity<Void> login(@RequestBody AdminLoginRequest request) {
        if (adminService.validatePassword(request.password())) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Catégories
    @GetMapping("/categories")
    @Operation(summary = "Get all categories for admin")
    public ResponseEntity<List<ArtworkCategoryDto>> getCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category")
    public ResponseEntity<ArtworkCategoryDto> createCategory(@Valid @RequestBody ArtworkCategoryDto dto) {
        return new ResponseEntity<>(adminService.createCategory(dto), HttpStatus.CREATED);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ArtworkCategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody ArtworkCategoryDto dto) {
        return ResponseEntity.ok(adminService.updateCategory(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Œuvres
    @GetMapping("/artworks")
    @Operation(summary = "Get all artworks for admin")
    public ResponseEntity<List<ArtworkDto>> getArtworks() {
        return ResponseEntity.ok(adminService.getAllArtworks());
    }

    @PostMapping("/artworks")
    @Operation(summary = "Create artwork")
    public ResponseEntity<ArtworkDto> createArtwork(@Valid @RequestBody ArtworkDto dto) {
        return new ResponseEntity<>(adminService.createArtwork(dto), HttpStatus.CREATED);
    }

    @PutMapping("/artworks/{id}")
    @Operation(summary = "Update artwork")
    public ResponseEntity<ArtworkDto> updateArtwork(@PathVariable Long id, @Valid @RequestBody ArtworkDto dto) {
        return ResponseEntity.ok(adminService.updateArtwork(id, dto));
    }

    @DeleteMapping("/artworks/{id}")
    @Operation(summary = "Delete artwork")
    public ResponseEntity<Void> deleteArtwork(@PathVariable Long id) {
        adminService.deleteArtwork(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/artworks/{id}/categories")
    @Operation(summary = "Update artwork categories")
    public ResponseEntity<ArtworkDto> updateArtworkCategories(
            @PathVariable Long id,
            @RequestBody Set<Long> categoryIds) {
        ArtworkDto updated = adminService.updateArtworkCategories(id, categoryIds);
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/artworks/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create artwork with optimized images")
    public ResponseEntity<ArtworkDto> createArtworkWithImages(
            @RequestPart("artwork") String artworkJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ArtworkDto artworkDto = mapper.readValue(artworkJson, ArtworkDto.class);

        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = new ArrayList<>();
            String categorySlug = getCategorySlug(artworkDto.getCategoryIds());

            for (MultipartFile image : images) {
                ImageService.ImageUploadResult result = imageService.uploadImage(image, categorySlug);
                uploadedUrls.add(result.imageUrl());
            }

            artworkDto.setImageUrls(uploadedUrls);
            if (!uploadedUrls.isEmpty()) {
                artworkDto.setMainImageUrl(uploadedUrls.get(0));
            }
        }

        ArtworkDto created = adminService.createArtwork(artworkDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping(value = "/artworks/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update artwork with optimized images")
    public ResponseEntity<ArtworkDto> updateArtworkWithImages(
            @PathVariable Long id,
            @RequestPart("artwork") String artworkJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ArtworkDto artworkDto = mapper.readValue(artworkJson, ArtworkDto.class);

        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = new ArrayList<>();
            String categorySlug = getCategorySlug(artworkDto.getCategoryIds());

            for (MultipartFile image : images) {
                ImageService.ImageUploadResult result = imageService.uploadImage(image, categorySlug);
                uploadedUrls.add(result.imageUrl());
            }

            List<String> existingUrls = artworkDto.getImageUrls() != null ?
                    new ArrayList<>(artworkDto.getImageUrls()) : new ArrayList<>();

            existingUrls.addAll(uploadedUrls);
            artworkDto.setImageUrls(existingUrls);

            if (artworkDto.getMainImageUrl() == null && !uploadedUrls.isEmpty()) {
                artworkDto.setMainImageUrl(uploadedUrls.get(0));
            }
        }

        ArtworkDto updated = adminService.updateArtwork(id, artworkDto);
        return ResponseEntity.ok(updated);
    }

    // Expositions
    @GetMapping("/exhibitions")
    @Operation(summary = "Get all exhibitions for admin")
    public ResponseEntity<List<ExhibitionDto>> getExhibitions() {
        return ResponseEntity.ok(adminService.getAllExhibitions());
    }

    @PostMapping("/exhibitions")
    @Operation(summary = "Create exhibition")
    public ResponseEntity<ExhibitionDto> createExhibition(@Valid @RequestBody ExhibitionDto dto) {
        return new ResponseEntity<>(adminService.createExhibition(dto), HttpStatus.CREATED);
    }

    @PutMapping("/exhibitions/{id}")
    @Operation(summary = "Update exhibition")
    public ResponseEntity<ExhibitionDto> updateExhibition(@PathVariable Long id, @Valid @RequestBody ExhibitionDto dto) {
        return ResponseEntity.ok(adminService.updateExhibition(id, dto));
    }

    @DeleteMapping("/exhibitions/{id}")
    @Operation(summary = "Delete exhibition")
    public ResponseEntity<Void> deleteExhibition(@PathVariable Long id) {
        adminService.deleteExhibition(id);
        return ResponseEntity.noContent().build();
    }

    // Messages de contact
    @GetMapping("/messages")
    @Operation(summary = "Get all contact messages")
    public ResponseEntity<List<ContactMessageDto>> getMessages() {
        return ResponseEntity.ok(adminService.getAllMessages());
    }

    @GetMapping("/messages/unread")
    @Operation(summary = "Get unread messages")
    public ResponseEntity<List<ContactMessageDto>> getUnreadMessages() {
        return ResponseEntity.ok(adminService.getUnreadMessages());
    }

    @GetMapping("/messages/count-unread")
    @Operation(summary = "Get unread messages count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(adminService.getUnreadMessagesCount());
    }

    @PutMapping("/messages/{id}/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<ContactMessageDto> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.markMessageAsRead(id));
    }

    @DeleteMapping("/messages/{id}")
    @Operation(summary = "Delete message")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        adminService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload optimized image")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category) throws IOException {

        ImageService.ImageUploadResult result = imageService.uploadImage(file, category);
        return ResponseEntity.ok(new ImageUploadResponse(result.imageUrl()));
    }

    @PostMapping("/upload/exhibition-image")
    @Operation(summary = "Upload exhibition image")
    public ResponseEntity<Map<String, String>> uploadExhibitionImage(
            @RequestParam("file") MultipartFile file) {

        try {
            ImageService.ImageUploadResult result = imageService.uploadImage(file, "expositions");

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", result.imageUrl());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error uploading exhibition image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'upload de l'image"));
        }
    }

    @PostMapping(value = "/upload/exhibition-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload exhibition video")
    public ResponseEntity<Map<String, String>> uploadExhibitionVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("exhibitionSlug") String exhibitionSlug,
            @RequestParam("videoIndex") int videoIndex) {

        try {
            ImageService.VideoUploadResult result = imageService.uploadVideo(file, exhibitionSlug, videoIndex);

            Map<String, String> response = new HashMap<>();
            response.put("videoUrl", result.videoUrl());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error uploading exhibition video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'upload de la vidéo"));
        }
    }

    @PostMapping(value = "/exhibitions/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create exhibition with images")
    public ResponseEntity<ExhibitionDto> createExhibitionWithImages(
            @RequestPart("exhibition") String exhibitionJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ExhibitionDto exhibitionDto = mapper.readValue(exhibitionJson, ExhibitionDto.class);

        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = new ArrayList<>();

            for (MultipartFile image : images) {
                ImageService.ImageUploadResult result = imageService.uploadImage(image, "exhibitions");
                uploadedUrls.add(result.imageUrl());
            }

            exhibitionDto.setImageUrls(uploadedUrls);
            if (!uploadedUrls.isEmpty()) {
                exhibitionDto.setImageUrl(uploadedUrls.get(0));
            }
        }

        ExhibitionDto created = adminService.createExhibition(exhibitionDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping(value = "/exhibitions/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update exhibition with images")
    public ResponseEntity<ExhibitionDto> updateExhibitionWithImages(
            @PathVariable Long id,
            @RequestPart("exhibition") String exhibitionJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ExhibitionDto exhibitionDto = mapper.readValue(exhibitionJson, ExhibitionDto.class);

        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = new ArrayList<>();

            for (MultipartFile image : images) {
                ImageService.ImageUploadResult result = imageService.uploadImage(image, "exhibitions");
                uploadedUrls.add(result.imageUrl());
            }

            List<String> existingUrls = exhibitionDto.getImageUrls() != null ?
                    new ArrayList<>(exhibitionDto.getImageUrls()) : new ArrayList<>();

            existingUrls.addAll(uploadedUrls);
            exhibitionDto.setImageUrls(existingUrls);

            if (exhibitionDto.getImageUrl() == null && !uploadedUrls.isEmpty()) {
                exhibitionDto.setImageUrl(uploadedUrls.get(0));
            }
        }

        ExhibitionDto updated = adminService.updateExhibition(id, exhibitionDto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/upload/exhibition-image-indexed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload exhibition image with index")
    public ResponseEntity<Map<String, String>> uploadExhibitionImageIndexed(
            @RequestParam("file") MultipartFile file,
            @RequestParam("exhibitionSlug") String exhibitionSlug,
            @RequestParam("imageIndex") int imageIndex) {

        try {
            ImageService.ImageUploadResult result = imageService.uploadExhibitionImage(file, exhibitionSlug, imageIndex);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", result.imageUrl());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error uploading exhibition image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'upload de l'image"));
        }
    }

    private String getCategorySlug(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return "general";
        }

        return adminService.getAllCategories().stream()
                .filter(cat -> categoryIds.contains(cat.getId()))
                .findFirst()
                .map(ArtworkCategoryDto::getSlug)
                .orElse("general");
    }

    private String sanitizeForPath(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\-_]", "-").toLowerCase();
    }

    public record AdminLoginRequest(String password) {}
    public record ImageUploadResponse(String imageUrl) {}
}