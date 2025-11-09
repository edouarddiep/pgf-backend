package com.pgf.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
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
import java.io.ByteArrayInputStream;
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

        byte[] optimizedImage = optimizeImage(file, MAX_WIDTH, MAX_HEIGHT, JPEG_QUALITY);
        byte[] thumbnail = createThumbnail(file, THUMBNAIL_SIZE);

        String supabaseFolder = mapCategoryToSupabaseFolder(categorySlug);
        String fileName = generateFileName(file.getOriginalFilename(), categorySlug);
        String thumbnailName = "thumb_" + fileName;

        String mainImageUrl = uploadToSupabase(optimizedImage, supabaseFolder + "/images", fileName);
        String thumbnailUrl = uploadToSupabase(thumbnail, supabaseFolder + "/thumbnails", thumbnailName);

        return new ImageUploadResult(mainImageUrl, thumbnailUrl);
    }

    public ImageUploadResult uploadExhibitionImage(MultipartFile file, String exhibitionSlug, int imageIndex) throws IOException {
        validateFile(file);

        byte[] optimizedImage = optimizeImage(file, MAX_WIDTH, MAX_HEIGHT, JPEG_QUALITY);

        String fileName = String.format("image-%d.jpg", imageIndex);
        String folder = "expositions/" + exhibitionSlug + "/images";

        String imageUrl = uploadToSupabase(optimizedImage, folder, fileName);

        return new ImageUploadResult(imageUrl, null);
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
            case "exhibitions" -> "exhibitions";
            default -> "nouvelles-oeuvres";
        };
    }

    private byte[] optimizeImage(MultipartFile file, int maxWidth, int maxHeight, float quality) throws IOException {
        byte[] fileBytes = file.getBytes();

        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(fileBytes));

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(fileBytes));
            ExifIFD0Directory exif = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (exif != null && exif.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = exif.getInt(ExifIFD0Directory.TAG_ORIENTATION);

                if (orientation == 3) {
                    originalImage = rotate(originalImage, 180);
                } else if (orientation == 6) {
                    originalImage = rotate(originalImage, 90);
                } else if (orientation == 8) {
                    originalImage = rotate(originalImage, -90);
                }

                log.info("Image orientation corrigée: {}", orientation);
            }
        } catch (Exception e) {
            log.warn("Pas de données EXIF: {}", e.getMessage());
        }

        Dimension newDim = calculateDimensions(originalImage.getWidth(), originalImage.getHeight(), maxWidth, maxHeight);

        BufferedImage resizedImage = new BufferedImage(newDim.width, newDim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, newDim.width, newDim.height, null);
        g2d.dispose();

        return compressToJpeg(resizedImage, quality);
    }

    private BufferedImage rotate(BufferedImage img, int angle) {
        int w = img.getWidth();
        int h = img.getHeight();

        int newW = (angle == 90 || angle == -90) ? h : w;
        int newH = (angle == 90 || angle == -90) ? w : h;

        BufferedImage rotated = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rotated.createGraphics();

        if (angle == 90) {
            g.translate(h, 0);
            g.rotate(Math.toRadians(90));
        } else if (angle == -90) {
            g.translate(0, w);
            g.rotate(Math.toRadians(-90));
        } else if (angle == 180) {
            g.translate(w, h);
            g.rotate(Math.toRadians(180));
        }

        g.drawImage(img, 0, 0, null);
        g.dispose();

        return rotated;
    }

    private byte[] createThumbnail(MultipartFile file, int size) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

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
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("x-upsert", "true");
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
            headers.set("Authorization", "Bearer " + serviceKey);
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

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    public VideoUploadResult uploadVideo(MultipartFile file, String exhibitionSlug, int videoIndex) throws IOException {
        validateVideoFile(file);

        String fileName = String.format("video-%d.mp4", videoIndex);
        String folder = "expositions/" + exhibitionSlug + "/videos";

        String videoUrl = uploadToSupabase(file.getBytes(), folder, fileName);

        return new VideoUploadResult(videoUrl);
    }

    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 500 * 1024 * 1024) {
            throw new IllegalArgumentException("Video too large (max 500MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("video/mp4")) {
            throw new IllegalArgumentException("Only MP4 videos are supported");
        }
    }

    public record VideoUploadResult(String videoUrl) {}

    public record ImageUploadResult(String imageUrl, String thumbnailUrl) {
    }
}