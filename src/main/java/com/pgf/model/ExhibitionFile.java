package com.pgf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "exhibition_files")
@Data
@EqualsAndHashCode(callSuper = true)
public class ExhibitionFile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exhibition_id", nullable = false)
    private Exhibition exhibition;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @NotBlank
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "title")
    private String title;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "source")
    private String source;

    @Column(name = "published_on")
    private LocalDate publishedOn;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    public void applyFileTypeDefault() {
        if (fileType == null) {
            fileType = FileType.fromMimeType(mimeType);
        }
    }

    public enum MediaType {
        PHOTO, VIDEO, AUDIO, PRESS_ARTICLE, INTERVIEW, DOCUMENT, OTHER
    }

    public enum FileType {
        IMAGE, VIDEO, AUDIO, PDF, DOCUMENT, LINK;

        public static FileType fromMimeType(String mimeType) {
            if (mimeType == null) {
                return DOCUMENT;
            }
            if (mimeType.startsWith("image/")) {
                return IMAGE;
            }
            if (mimeType.startsWith("video/")) {
                return VIDEO;
            }
            if (mimeType.startsWith("audio/")) {
                return AUDIO;
            }
            if (mimeType.startsWith("application/pdf")) {
                return PDF;
            }
            return DOCUMENT;
        }
    }
}
