package com.pgf.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArtworkCategoryDto {

    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String slug;

    private Integer displayOrder;

    private List<ArtworkDto> artworks;

    private Long artworkCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}