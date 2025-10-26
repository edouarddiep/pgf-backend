package com.pgf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArtworkCategoryDto {

    private Long id;

    @NotBlank(message = "Le nom de la catÃ©gorie est requis")
    private String name;

    private String description;

    private String descriptionShort;

    @NotBlank(message = "Le slug est requis")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Le slug ne peut contenir que des lettres minuscules, des chiffres et des tirets")
    private String slug;

    private Integer displayOrder = 0;

    private String mainImageUrl;

    private String thumbnailUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}