package com.pgf.controller.admin;

import com.pgf.dto.*;
import com.pgf.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Admin", description = "Admin management endpoints")
@RequiredArgsConstructor
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

    // Gestion des catégories d'œuvres (Many-to-Many)
    @PutMapping("/artworks/{id}/categories")
    @Operation(summary = "Update artwork categories")
    public ResponseEntity<ArtworkDto> updateArtworkCategories(
            @PathVariable Long id,
            @RequestBody Set<Long> categoryIds) {
        ArtworkDto updated = adminService.updateArtworkCategories(id, categoryIds);
        return ResponseEntity.ok(updated);
    }

    // Upload avec images multiples
    @PostMapping(value = "/artworks/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create artwork with images")
    public ResponseEntity<ArtworkDto> createArtworkWithImages(
            @RequestPart("artwork") @Valid ArtworkDto artworkDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = new ArrayList<>();
            String folderName = sanitizeForPath(artworkDto.getTitle());

            for (MultipartFile image : images) {
                String imageUrl = imageService.uploadImage(image, folderName);
                uploadedUrls.add(imageUrl);
            }

            artworkDto.setImageUrls(uploadedUrls);
            if (!uploadedUrls.isEmpty()) {
                artworkDto.setMainImageUrl(uploadedUrls.get(0));
            }
        }

        ArtworkDto created = adminService.createArtwork(artworkDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
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

    // Upload d'images
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category) throws IOException {
        String imageUrl = imageService.uploadImage(file, category);
        return ResponseEntity.ok(new ImageUploadResponse(imageUrl));
    }

    private String sanitizeForPath(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\-_]", "-").toLowerCase();
    }

    public record AdminLoginRequest(String password) {}
    public record ImageUploadResponse(String imageUrl) {}
}