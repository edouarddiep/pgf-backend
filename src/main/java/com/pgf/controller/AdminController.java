package com.pgf.controller;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.dto.ArtworkDto;
import com.pgf.dto.ContactMessageDto;
import com.pgf.dto.ExhibitionDto;
import com.pgf.service.AdminService;
import com.pgf.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@Tag(name = "Admin", description = "Administration endpoints")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ImageService imageService;

    // === AUTHENTICATION ===

    @PostMapping("/auth/login")
    @Operation(summary = "Admin login")
    public ResponseEntity<Void> login(@RequestBody Map<String, String> credentials) {
        String password = credentials.get("password");
        if (adminService.validatePassword(password)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // === IMAGE UPLOAD ===

    @PostMapping("/upload/image")
    @Operation(summary = "Upload image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "artworks") String category) {
        try {
            String imageUrl = imageService.uploadImage(file, category);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === ARTWORKS MANAGEMENT ===

    @GetMapping("/artworks")
    @Operation(summary = "Get all artworks for admin")
    public ResponseEntity<List<ArtworkDto>> getArtworks() {
        List<ArtworkDto> artworks = adminService.getAllArtworks();
        return ResponseEntity.ok(artworks);
    }

    @PostMapping("/artworks")
    @Operation(summary = "Create artwork")
    public ResponseEntity<ArtworkDto> createArtwork(@Valid @RequestBody ArtworkDto artworkDto) {
        ArtworkDto created = adminService.createArtwork(artworkDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/artworks/{id}")
    @Operation(summary = "Update artwork")
    public ResponseEntity<ArtworkDto> updateArtwork(@PathVariable Long id, @Valid @RequestBody ArtworkDto artworkDto) {
        ArtworkDto updated = adminService.updateArtwork(id, artworkDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/artworks/{id}")
    @Operation(summary = "Delete artwork")
    public ResponseEntity<Void> deleteArtwork(@PathVariable Long id) {
        adminService.deleteArtwork(id);
        return ResponseEntity.noContent().build();
    }

    // === CATEGORIES MANAGEMENT ===

    @GetMapping("/categories")
    @Operation(summary = "Get all categories for admin")
    public ResponseEntity<List<ArtworkCategoryDto>> getCategories() {
        List<ArtworkCategoryDto> categories = adminService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category")
    public ResponseEntity<ArtworkCategoryDto> createCategory(@Valid @RequestBody ArtworkCategoryDto categoryDto) {
        ArtworkCategoryDto created = adminService.createCategory(categoryDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ArtworkCategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody ArtworkCategoryDto categoryDto) {
        ArtworkCategoryDto updated = adminService.updateCategory(id, categoryDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Exhibitions
    @GetMapping("/exhibitions")
    @Operation(summary = "Get all exhibitions for admin")
    public ResponseEntity<List<ExhibitionDto>> getExhibitions() {
        return ResponseEntity.ok(adminService.getAllExhibitions());
    }

    @PostMapping("/exhibitions")
    @Operation(summary = "Create exhibition")
    public ResponseEntity<ExhibitionDto> createExhibition(@Valid @RequestBody ExhibitionDto dto) {
        ExhibitionDto created = adminService.createExhibition(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/exhibitions/{id}")
    @Operation(summary = "Update exhibition")
    public ResponseEntity<ExhibitionDto> updateExhibition(@PathVariable Long id, @Valid @RequestBody ExhibitionDto dto) {
        ExhibitionDto updated = adminService.updateExhibition(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/exhibitions/{id}")
    @Operation(summary = "Delete exhibition")
    public ResponseEntity<Void> deleteExhibition(@PathVariable Long id) {
        adminService.deleteExhibition(id);
        return ResponseEntity.noContent().build();
    }

    // Messages
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
    public ResponseEntity<Long> getUnreadMessagesCount() {
        return ResponseEntity.ok(adminService.getUnreadMessagesCount());
    }

    @PutMapping("/messages/{id}/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<ContactMessageDto> markMessageAsRead(@PathVariable Long id) {
        ContactMessageDto updated = adminService.markMessageAsRead(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/messages/{id}")
    @Operation(summary = "Delete message")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        adminService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}