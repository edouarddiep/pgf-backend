package com.pgf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Value("${app.upload.dir:${user.home}/pgf-uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:10485760}") // 10MB par défaut
    private long maxFileSize;

    private final List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff"
    );

    private final List<String> allowedContentTypes = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "image/bmp", "image/tiff"
    );

    /**
     * Upload une image et retourne l'URL d'accès
     */
    public String uploadImage(MultipartFile file, String category) throws IOException {
        validateFile(file);

        // Créer le répertoire de destination
        Path uploadPath = createUploadDirectory(category);

        // Générer un nom de fichier unique
        String fileName = generateUniqueFileName(file.getOriginalFilename());

        // Chemin complet du fichier
        Path filePath = uploadPath.resolve(fileName);

        // Copier le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Image uploaded successfully: {}", filePath);

        // Retourner l'URL relative pour l'API
        return "/images/" + category + "/" + fileName;
    }

    /**
     * Upload une image avec génération automatique de thumbnail
     */
    public ImageUploadResult uploadImageWithThumbnail(MultipartFile file, String category) throws IOException {
        validateFile(file);

        Path uploadPath = createUploadDirectory(category);
        String fileName = generateUniqueFileName(file.getOriginalFilename());

        // Image principale
        Path mainImagePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), mainImagePath, StandardCopyOption.REPLACE_EXISTING);

        // Thumbnail (pour l'instant, même image - peut être amélioré avec redimensionnement)
        String thumbnailFileName = "thumb_" + fileName;
        Path thumbnailPath = uploadPath.resolve(thumbnailFileName);
        Files.copy(file.getInputStream(), thumbnailPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Image and thumbnail uploaded: {} and {}", mainImagePath, thumbnailPath);

        return ImageUploadResult.builder()
                .imageUrl("/images/" + category + "/" + fileName)
                .thumbnailUrl("/images/" + category + "/" + thumbnailFileName)
                .fileName(fileName)
                .fileSize(file.getSize())
                .build();
    }

    /**
     * Supprimer une image
     */
    public boolean deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.startsWith("/images/")) {
                return false;
            }

            // Convertir l'URL en chemin de fichier
            String relativePath = imageUrl.substring("/images/".length());
            Path filePath = Paths.get(uploadDir, "images", relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Image deleted: {}", filePath);

                // Essayer de supprimer le thumbnail associé
                String thumbPath = relativePath.replace("/", "/thumb_");
                Path thumbnailPath = Paths.get(uploadDir, "images", thumbPath);
                if (Files.exists(thumbnailPath)) {
                    Files.delete(thumbnailPath);
                    log.info("Thumbnail deleted: {}", thumbnailPath);
                }

                return true;
            }
        } catch (IOException e) {
            log.error("Error deleting image: {}", imageUrl, e);
        }
        return false;
    }

    /**
     * Vérifier si un fichier image existe
     */
    public boolean imageExists(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/images/")) {
            return false;
        }

        String relativePath = imageUrl.substring("/images/".length());
        Path filePath = Paths.get(uploadDir, "images", relativePath);
        return Files.exists(filePath);
    }

    /**
     * Obtenir le chemin physique d'une image
     */
    public Path getImagePath(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/images/")) {
            return null;
        }

        String relativePath = imageUrl.substring("/images/".length());
        return Paths.get(uploadDir, "images", relativePath);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier ne peut pas être vide");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Le fichier est trop volumineux. Taille maximum: " +
                    (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Types acceptés: " +
                    String.join(", ", allowedContentTypes));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("Extension de fichier non autorisée. Extensions acceptées: " +
                        String.join(", ", allowedExtensions));
            }
        }
    }

    private Path createUploadDirectory(String category) throws IOException {
        Path uploadPath = Paths.get(uploadDir, "images", category);
        Files.createDirectories(uploadPath);
        return uploadPath;
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);

        return String.format("pgf_%s_%s.%s", timestamp, uuid, extension);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // Classe pour le résultat d'upload avec thumbnail
    @lombok.Data
    @lombok.Builder
    public static class ImageUploadResult {
        private String imageUrl;
        private String thumbnailUrl;
        private String fileName;
        private long fileSize;
    }
}