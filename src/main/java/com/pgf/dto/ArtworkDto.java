package com.pgf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
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

    private String titleEn;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    private String descriptionEn;

    private List<String> imageUrls;

    private String mainImageUrl;

    private Integer mainImagePositionX = 50;
    private Integer mainImagePositionY = 50;
    private Integer mainImageZoom = 100;

    @JsonProperty("categoryIds")
    @JsonDeserialize(as = HashSet.class)
    private Set<Long> categoryIds;

    private Set<String> categoryNames;
    private Set<String> categorySlugs;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}