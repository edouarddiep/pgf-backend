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

    @Value("${app.upload.max-size:10485760}")
    private long maxFileSize;

    @Value("${app.upload.base-url:http://localhost:8080/api/images}")
    private String baseUrl;

    private final List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff"
    );

    private final List<String> allowedContentTypes = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "image/bmp", "image/tiff"
    );

    public String uploadImage(MultipartFile file, String category) throws IOException {
        validateFile(file);
        Path uploadPath = createUploadDirectory(category);
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Image uploaded: {}", filePath);

        return String.format("%s/%s/%s", baseUrl, category, fileName);
    }

    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || !imageUrl.startsWith(baseUrl)) {
            return;
        }

        String relativePath = imageUrl.substring(baseUrl.length() + 1);
        Path filePath = Paths.get(uploadDir, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Image deleted: {}", filePath);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File too large. Max size: " + maxFileSize + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Allowed: " + allowedContentTypes);
        }

        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = getFileExtension(fileName).toLowerCase();
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("Invalid file extension. Allowed: " + allowedExtensions);
            }
        }
    }

    private Path createUploadDirectory(String category) throws IOException {
        Path uploadPath = Paths.get(uploadDir, category);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}