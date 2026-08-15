package com.pgf.controller.admin;

import com.pgf.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/admin/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Tag(name = "Admin - Uploads", description = "Media upload endpoints")
@RequiredArgsConstructor
public class AdminUploadController {

    private static final String EXHIBITION_FOLDER = "expositions";

    private final FileUploadService fileUploadService;

    @PostMapping("/image")
    @Operation(summary = "Upload an artwork image")
    public ImageUploadResponse uploadImage(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "category", defaultValue = "general") String category) throws IOException {
        return ImageUploadResponse.of(fileUploadService.uploadImage(file, category));
    }

    @PostMapping("/category-image")
    @Operation(summary = "Upload a category thumbnail")
    public ImageUploadResponse uploadCategoryImage(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("categorySlug") String categorySlug) throws IOException {
        return ImageUploadResponse.of(fileUploadService.uploadImage(file, categorySlug));
    }

    @PostMapping("/exhibition-image")
    @Operation(summary = "Upload an exhibition image")
    public ImageUploadResponse uploadExhibitionImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ImageUploadResponse.of(fileUploadService.uploadImage(file, EXHIBITION_FOLDER));
    }

    @PostMapping("/exhibition-image-indexed")
    @Operation(summary = "Upload an exhibition image at a given index")
    public ImageUploadResponse uploadExhibitionImage(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("exhibitionSlug") String exhibitionSlug,
                                                      @RequestParam("imageIndex") int imageIndex) throws IOException {
        return ImageUploadResponse.of(fileUploadService.uploadExhibitionImage(file, exhibitionSlug, imageIndex));
    }

    @PostMapping("/exhibition-video")
    @Operation(summary = "Upload an exhibition video")
    public VideoUploadResponse uploadExhibitionVideo(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("exhibitionSlug") String exhibitionSlug,
                                                      @RequestParam("videoIndex") int videoIndex) throws IOException {
        return new VideoUploadResponse(fileUploadService.uploadVideo(file, exhibitionSlug, videoIndex).videoUrl());
    }

    @PostMapping("/file")
    @Operation(summary = "Upload a raw file (PDF, audio, video) without processing")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "folder", defaultValue = "archives") String folder) throws IOException {
        return new FileUploadResponse(fileUploadService.uploadFile(file, folder));
    }

    public record ImageUploadResponse(String imageUrl, String thumbnailUrl) {

        static ImageUploadResponse of(FileUploadService.ImageUploadResult result) {
            return new ImageUploadResponse(result.imageUrl(), result.thumbnailUrl());
        }
    }

    public record VideoUploadResponse(String videoUrl) {}

    public record FileUploadResponse(String fileUrl) {}
}
