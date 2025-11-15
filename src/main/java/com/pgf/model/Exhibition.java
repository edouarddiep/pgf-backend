package com.pgf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "exhibitions")
@Data
@EqualsAndHashCode(callSuper = true)
public class Exhibition extends BaseEntity {

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "address")
    private String address;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_urls", columnDefinition = "TEXT[]")
    private List<String> imageUrls;

    @Column(name = "video_urls", columnDefinition = "TEXT[]")
    private List<String> videoUrls;

    @Column(name = "credits", columnDefinition = "TEXT")
    private String credits;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExhibitionStatus status = ExhibitionStatus.UPCOMING;

    public enum ExhibitionStatus {
        UPCOMING,
        ONGOING,
        PAST
    }
}