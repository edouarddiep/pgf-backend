package com.pgf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ArtworkDto {

    private Long id;

    @NotBlank
    private String title;

    private String description;

    private String dimensions;

    private String materials;

    private LocalDate creationDate;

    private BigDecimal price;

    private Boolean isAvailable;

    private String imageUrl;

    private String thumbnailUrl;

    private Integer displayOrder;

    @NotNull
    private Long categoryId;

    private String categoryName;

    private String categorySlug;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}