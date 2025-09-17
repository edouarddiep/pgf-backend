package com.pgf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkDto {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String title;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Size(max = 255, message = "Les dimensions ne peuvent pas dépasser 255 caractères")
    private String dimensions;

    @Size(max = 255, message = "Les matériaux ne peuvent pas dépasser 255 caractères")
    private String materials;

    private LocalDate creationDate;

    @Positive(message = "Le prix doit être positif")
    private BigDecimal price;

    @NotNull(message = "La disponibilité est obligatoire")
    private Boolean isAvailable;

    private List<String> imageUrls;

    private String mainImageUrl;

    private Integer displayOrder;

    private Set<Long> categoryIds;
    private Set<String> categoryNames;
    private Set<String> categorySlugs;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}