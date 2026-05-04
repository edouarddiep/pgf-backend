package com.pgf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "archives")
@Data
@EqualsAndHashCode(callSuper = true)
public class Archive extends BaseEntity {

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_en")
    private String titleEn;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "main_image_position_x")
    private Integer mainImagePositionX;

    @Column(name = "main_image_position_y")
    private Integer mainImagePositionY;

    @Column(name = "main_image_zoom")
    private Integer mainImageZoom;

    @OneToMany(mappedBy = "archive", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fileName ASC")
    private List<ArchiveFile> files = new ArrayList<>();
}