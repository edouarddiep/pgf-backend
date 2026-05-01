package com.pgf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkCategoryDto {

    private Long id;

    @NotBlank(message = "Le nom de la catégorie est requis")
    private String name;

    private String nameEn;

    private String description;

    private String descriptionEn;

    @NotBlank(message = "Le slug est requis")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Le slug ne peut contenir que des lettres minuscules, des chiffres et des tirets")
    private String slug;

    private String thumbnailUrl;
    private Integer thumbnailPositionX;
    private Integer thumbnailPositionY;
    private Integer thumbnailZoom;
    private Integer displayOrder;
    private Integer artworkCount;
}