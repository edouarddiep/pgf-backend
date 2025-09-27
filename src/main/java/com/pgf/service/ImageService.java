package com.pgf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${app.upload.supabase.bucket:oeuvres}")
    private String bucketName;

    private final RestTemplate restTemplate = new RestTemplate();

    private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
    private final int MAX_WIDTH = 1200;
    private final int MAX_HEIGHT = 800;
    private final float JPEG_QUALITY = 0.85f;
    private final int THUMBNAIL_SIZE = 300;

    public ImageUploadResult uploadImage(MultipartFile file, String categorySlug) throws IOException {
        validateFile(file);

        // Optimiser l'image principale
        byte[] optimizedImage = optimizeImage(file, MAX_WIDTH, MAX_HEIGHT, JPEG_QUALITY);

        // Créer une miniature
        byte[] thumbnail = createThumbnail(file, THUMBNAIL_SIZE);

        // Mapper vers la structure existante
        String supabaseFolder = mapCategoryToSupabaseFolder(categorySlug);
        String fileName = generateFileName(file.getOriginalFilename(), categorySlug);
        String thumbnailName = "thumb_" + fileName;

        // Upload image principale
        String mainImageUrl = uploadToSupabase(optimizedImage, supabaseFolder + "/images", fileName);

        // Upload miniature
        String thumbnailUrl = uploadToSupabase(thumbnail, supabaseFolder + "/thumbnails", thumbnailName);

        return new ImageUploadResult(mainImageUrl, thumbnailUrl);
    }

    private String mapCategoryToSupabaseFolder(String categorySlug) {
        return switch (categorySlug.toLowerCase()) {
            case "collages-dessins" -> "collages-dessins";
            case "fils-de-fer" -> "fils-de-fer";
            case "land-art" -> "land-art";
            case "livres-objets" -> "livre-objet";
            case "papiers-japonais" -> "papier-japonais";
            case "peintures" -> "peinture";
            case "sacs-colliers" -> "sacs-colliers";
            case "sculptures" -> "sculpture";
            case "toiles-jute" -> "toile-de-jute";
            case "yaya" -> "yaya";
            default -> "nouvelles-oeuvres"; // Fallback pour nouvelles catégories
        };
    }

    private byte[] optimizeImage(MultipartFile file, int maxWidth, int maxHeight, float quality) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // Calculer nouvelles dimensions en gardant les proportions
        Dimension newDim = calculateDimensions(originalImage.getWidth(), originalImage.getHeight(), maxWidth, maxHeight);

        // Redimensionner avec qualité élevée
        BufferedImage resizedImage = new BufferedImage(newDim.width, newDim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, newDim.width, newDim.height, null);
        g2d.dispose();

        // Compresser en JPEG avec qualité spécifiée
        return compressToJpeg(resizedImage, quality);
    }

    private byte[] createThumbnail(MultipartFile file, int size) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // Créer une miniature carrée avec crop centré
        int sourceSize = Math.min(originalImage.getWidth(), originalImage.getHeight());
        int x = (originalImage.getWidth() - sourceSize) / 2;
        int y = (originalImage.getHeight() - sourceSize) / 2;

        BufferedImage croppedImage = originalImage.getSubimage(x, y, sourceSize, sourceSize);

        BufferedImage thumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(croppedImage, 0, 0, size, size, null);
        g2d.dispose();

        return compressToJpeg(thumbnail, 0.8f);
    }

    private byte[] compressToJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam jpegWriteParam = jpegWriter.getDefaultWriteParam();
        jpegWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegWriteParam.setCompressionQuality(quality);

        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
            jpegWriter.setOutput(imageOutputStream);
            jpegWriter.write(null, new javax.imageio.IIOImage(image, null, null), jpegWriteParam);
        }
        jpegWriter.dispose();

        return outputStream.toByteArray();
    }

    private Dimension calculateDimensions(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        if (ratio >= 1.0) {
            return new Dimension(originalWidth, originalHeight);
        }

        return new Dimension(
                (int) (originalWidth * ratio),
                (int) (originalHeight * ratio)
        );
    }

    private String uploadToSupabase(byte[] imageData, String folder, String fileName) throws IOException {
        String filePath = folder + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(imageData.length);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageData, headers);

        String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, filePath);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, filePath);
                log.info("Image uploaded to Supabase: {}", publicUrl);
                return publicUrl;
            } else {
                throw new RuntimeException("Failed to upload to Supabase Storage: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error uploading to Supabase: {}", e.getMessage(), e);
            throw new IOException("Upload failed: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains(supabaseUrl)) {
            return;
        }

        try {
            String filePath = extractFilePathFromUrl(imageUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(serviceKey);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String deleteUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, filePath);

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, Void.class);
            log.info("Image deleted from Supabase: {}", filePath);

        } catch (Exception e) {
            log.error("Error deleting image from Supabase: {}", imageUrl, e);
        }
    }

    private String extractFilePathFromUrl(String url) {
        String publicPath = "/storage/v1/object/public/" + bucketName + "/";
        int index = url.indexOf(publicPath);
        if (index != -1) {
            return url.substring(index + publicPath.length());
        }
        return "";
    }

    private String generateFileName(String originalFileName, String categorySlug) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s-%s-%s.jpg", categorySlug, timestamp, uuid);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 50 * 1024 * 1024) { // 50MB max
            throw new IllegalArgumentException("File too large");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    public record ImageUploadResult(String imageUrl, String thumbnailUrl) {

    }
}