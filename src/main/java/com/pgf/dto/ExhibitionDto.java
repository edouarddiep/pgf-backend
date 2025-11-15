package com.pgf.dto;

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

    private String description;

    private String location;

    private String address;

    private LocalDate startDate;

    private LocalDate endDate;

    private String imageUrl;

    private List<String> imageUrls;

    private List<String> videoUrls;

    private Exhibition.ExhibitionStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String credits;

    private String websiteUrl;
}