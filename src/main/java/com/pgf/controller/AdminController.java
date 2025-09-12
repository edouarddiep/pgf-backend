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
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Admin", description = "Admin management endpoints")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthService authService;
    private final ArtworkCategoryService categoryService;
    private final ArtworkService artworkService;
    private final ExhibitionService exhibitionService;
    private final ContactMessageService messageService;
    private final ImageService imageService;

    @PostMapping("/auth/login")
    @Operation(summary = "Admin login")
    public ResponseEntity<Void> login(@RequestBody AdminLoginRequest request) {
        authService.authenticate(request.password());
        return ResponseEntity.ok().build();
    }

    // Catégories
    @GetMapping("/categories")
    @Operation(summary = "Get all categories for admin")
    public ResponseEntity<List<ArtworkCategoryDto>> getCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category")
    public ResponseEntity<ArtworkCategoryDto> createCategory(@Valid @RequestBody ArtworkCategoryDto dto) {
        return new ResponseEntity<>(categoryService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ArtworkCategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody ArtworkCategoryDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Œuvres
    @GetMapping("/artworks")
    @Operation(summary = "Get all artworks for admin")
    public ResponseEntity<List<ArtworkDto>> getArtworks() {
        return ResponseEntity.ok(artworkService.findAll());
    }

    @GetMapping("/artworks/category/{categoryId}")
    @Operation(summary = "Get artworks by category for admin")
    public ResponseEntity<List<ArtworkDto>> getArtworksByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(artworkService.findByCategoryId(categoryId));
    }

    @PostMapping("/artworks")
    @Operation(summary = "Create artwork")
    public ResponseEntity<ArtworkDto> createArtwork(@Valid @RequestBody ArtworkDto dto) {
        return new ResponseEntity<>(artworkService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/artworks/{id}")
    @Operation(summary = "Update artwork")
    public ResponseEntity<ArtworkDto> updateArtwork(@PathVariable Long id, @Valid @RequestBody ArtworkDto dto) {
        return ResponseEntity.ok(artworkService.update(id, dto));
    }

    @DeleteMapping("/artworks/{id}")
    @Operation(summary = "Delete artwork")
    public ResponseEntity<Void> deleteArtwork(@PathVariable Long id) {
        artworkService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Expositions
    @GetMapping("/exhibitions")
    @Operation(summary = "Get all exhibitions for admin")
    public ResponseEntity<List<ExhibitionDto>> getExhibitions() {
        return ResponseEntity.ok(exhibitionService.findAll());
    }

    @PostMapping("/exhibitions")
    @Operation(summary = "Create exhibition")
    public ResponseEntity<ExhibitionDto> createExhibition(@Valid @RequestBody ExhibitionDto dto) {
        return new ResponseEntity<>(exhibitionService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/exhibitions/{id}")
    @Operation(summary = "Update exhibition")
    public ResponseEntity<ExhibitionDto> updateExhibition(@PathVariable Long id, @Valid @RequestBody ExhibitionDto dto) {
        return ResponseEntity.ok(exhibitionService.update(id, dto));
    }

    @DeleteMapping("/exhibitions/{id}")
    @Operation(summary = "Delete exhibition")
    public ResponseEntity<Void> deleteExhibition(@PathVariable Long id) {
        exhibitionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Messages de contact
    @GetMapping("/messages")
    @Operation(summary = "Get all contact messages")
    public ResponseEntity<List<ContactMessageDto>> getMessages() {
        return ResponseEntity.ok(messageService.findAll());
    }

    @GetMapping("/messages/unread")
    @Operation(summary = "Get unread messages")
    public ResponseEntity<List<ContactMessageDto>> getUnreadMessages() {
        return ResponseEntity.ok(messageService.findUnreadMessages());
    }

    @GetMapping("/messages/count-unread")
    @Operation(summary = "Get unread messages count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(messageService.countUnreadMessages());
    }

    @PutMapping("/messages/{id}/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<ContactMessageDto> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }

    @DeleteMapping("/messages/{id}")
    @Operation(summary = "Delete message")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.delete(id);
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

    public record AdminLoginRequest(String password) {}
    public record ImageUploadResponse(String imageUrl) {}
}