package com.pgf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pgf.model.Exhibition;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExhibitionDto {

    private Long id;

    @NotBlank
    private String title;

    private String titleEn;

    private String description;

    private String descriptionEn;

    private String location;

    private String address;

    private LocalDate startDate;

    private LocalDate endDate;

    private String imageUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageSrcset;

    private List<String> imageUrls;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> imageSrcsets;

    private List<String> videoUrls;

    private Exhibition.ExhibitionStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String credits;

    private String vernissageUrl;

    private String websiteUrl;
}