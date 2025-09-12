package com.pgf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArtworkDto {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String title;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @NotNull(message = "Le statut de disponibilité est obligatoire")
    private Boolean isAvailable;

    @NotNull(message = "Au moins une image est requise")
    @Size(min = 1, message = "Au moins une image est requise")
    private List<String> imageUrls;

    private Integer displayOrder;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    private String categoryName;
    private String categorySlug;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}