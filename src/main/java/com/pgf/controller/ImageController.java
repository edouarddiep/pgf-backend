package com.pgf.controller;

import com.pgf.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Images", description = "Gestion des images et fichiers")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload/image")
    @Operation(summary = "Upload une image simple")
    public ResponseEntity<Map<String, String>> uploadImage(
            @Parameter(description = "Fichier image à uploader")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Catégorie pour organiser les images")
            @RequestParam(value = "category", defaultValue = "general") String category) {

        try {
            String imageUrl = imageService.uploadImage(file, category);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploadée avec succès");
            response.put("fileName", file.getOriginalFilename());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (IOException e) {
            log.error("Erreur lors de l'upload de l'image", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de l'upload du fichier");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/upload/image-with-thumbnail")
    @Operation(summary = "Upload une image avec génération de thumbnail")
    public ResponseEntity<Map<String, Object>> uploadImageWithThumbnail(
            @Parameter(description = "Fichier image à uploader")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Catégorie pour organiser les images")
            @RequestParam(value = "category", defaultValue = "general") String category) {

        try {
            ImageService.ImageUploadResult result = imageService.uploadImageWithThumbnail(file, category);

            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", result.getImageUrl());
            response.put("thumbnailUrl", result.getThumbnailUrl());
            response.put("fileName", result.getFileName());
            response.put("fileSize", result.getFileSize());
            response.put("message", "Image et thumbnail uploadés avec succès");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (IOException e) {
            log.error("Erreur lors de l'upload de l'image avec thumbnail", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de l'upload du fichier");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/images/{category}/{filename:.+}")
    @Operation(summary = "Récupérer une image par son nom de fichier")
    public ResponseEntity<Resource> getImage(
            @PathVariable String category,
            @PathVariable String filename) {

        try {
            String imageUrl = "/images/" + category + "/" + filename;
            Path imagePath = imageService.getImagePath(imageUrl);

            if (imagePath == null || !imageService.imageExists(imageUrl)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(imagePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Déterminer le type de contenu
            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("URL malformée pour l'image: {}/{}", category, filename, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/images")
    @Operation(summary = "Supprimer une image par son URL")
    public ResponseEntity<Map<String, String>> deleteImage(
            @Parameter(description = "URL de l'image à supprimer")
            @RequestParam String imageUrl) {

        boolean deleted = imageService.deleteImage(imageUrl);
        Map<String, String> response = new HashMap<>();

        if (deleted) {
            response.put("message", "Image supprimée avec succès");
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Image non trouvée ou erreur lors de la suppression");
            response.put("imageUrl", imageUrl);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/images/exists")
    @Operation(summary = "Vérifier l'existence d'une image")
    public ResponseEntity<Map<String, Object>> checkImageExists(
            @Parameter(description = "URL de l'image à vérifier")
            @RequestParam String imageUrl) {

        boolean exists = imageService.imageExists(imageUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "tiff", "tif" -> "image/tiff";
            default -> "application/octet-stream";
        };
    }
}