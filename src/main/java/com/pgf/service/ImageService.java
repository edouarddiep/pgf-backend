package com.pgf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
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
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Value("${app.upload.dir:${user.home}/pgf-uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:52428800}")
    private long maxFileSize;

    @Value("${app.upload.base-url:http://localhost:8080/api/images}")
    private String baseUrl;

    @Value("${app.upload.use-supabase-storage:false}")
    private boolean useSupabaseStorage;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.service-key:}")
    private String serviceKey;

    @Value("${app.upload.supabase.bucket:oeuvres}")
    private String bucketName;

    private final RestTemplate restTemplate = new RestTemplate();

    private final List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff", "mp4", "webm"
    );

    private final List<String> allowedContentTypes = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "image/bmp", "image/tiff", "video/mp4", "video/webm"
    );

    public String uploadImage(MultipartFile file, String categorySlug) throws IOException {
        validateFile(file);

        if (useSupabaseStorage && !supabaseUrl.isEmpty() && !serviceKey.isEmpty()) {
            try {
                return uploadToSupabase(file, categorySlug);
            } catch (Exception e) {
                log.warn("Supabase upload failed, falling back to local storage", e);
                return uploadLocally(file, categorySlug);
            }
        } else {
            return uploadLocally(file, categorySlug);
        }
    }

    public String getOptimizedImageUrl(String imagePath, ImageSize size) {
        if (useSupabaseStorage && imagePath.contains(supabaseUrl)) {
            return getSupabaseOptimizedUrl(imagePath, size);
        }
        return imagePath; // Return as-is for local or already full URLs
    }

    private String uploadToSupabase(MultipartFile file, String categorySlug) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename(), categorySlug);
        String filePath = String.format("%s/%s", categorySlug, fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucketName, filePath);

        ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String publicUrl = String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseUrl, bucketName, filePath);
            log.info("Image uploaded to Supabase: {}", publicUrl);
            return publicUrl;
        } else {
            throw new RuntimeException("Failed to upload to Supabase Storage");
        }
    }

    private String uploadLocally(MultipartFile file, String categorySlug) throws IOException {
        Path uploadPath = createUploadDirectory(categorySlug);
        String fileName = generateUniqueFileName(file.getOriginalFilename(), categorySlug);
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Image uploaded locally: {}", filePath);

        return String.format("%s/%s/%s", baseUrl, categorySlug, fileName);
    }

    private String getSupabaseOptimizedUrl(String originalUrl, ImageSize size) {
        return switch (size) {
            case THUMBNAIL -> originalUrl + "?width=300&height=300&resize=cover&quality=80";
            case MEDIUM -> originalUrl + "?width=800&height=600&resize=cover&quality=85";
            case LARGE -> originalUrl + "?width=1200&height=900&resize=cover&quality=90";
            case ORIGINAL -> originalUrl;
        };
    }

    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null) {
            return;
        }

        if (useSupabaseStorage && imageUrl.contains(supabaseUrl)) {
            deleteFromSupabase(imageUrl);
        } else if (imageUrl.startsWith(baseUrl)) {
            deleteFromLocal(imageUrl);
        }
    }

    private void deleteFromSupabase(String imageUrl) {
        try {
            String filePath = extractFilePathFromSupabaseUrl(imageUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(serviceKey);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String deleteUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseUrl, bucketName, filePath);

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, Void.class);
            log.info("Image deleted from Supabase: {}", filePath);

        } catch (Exception e) {
            log.error("Error deleting file from Supabase Storage: {}", imageUrl, e);
        }
    }

    private void deleteFromLocal(String imageUrl) throws IOException {
        String relativePath = imageUrl.substring(baseUrl.length() + 1);
        Path filePath = Paths.get(uploadDir, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Image deleted locally: {}", filePath);
        }
    }

    private String extractFilePathFromSupabaseUrl(String url) {
        String publicPath = "/storage/v1/object/public/" + bucketName + "/";
        int index = url.indexOf(publicPath);
        if (index != -1) {
            return url.substring(index + publicPath.length());
        }
        return "";
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

    private Path createUploadDirectory(String categorySlug) throws IOException {
        Path uploadPath = Paths.get(uploadDir, categorySlug);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    private String generateUniqueFileName(String originalFileName, String categorySlug) {
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        // Inclure la catégorie dans le nom pour éviter les conflits
        return String.format("%s-%s-%s.%s",
                categorySlug.replaceAll("[^a-z0-9-]", ""), timestamp, uuid, extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public enum ImageSize {
        THUMBNAIL, MEDIUM, LARGE, ORIGINAL
    }
}