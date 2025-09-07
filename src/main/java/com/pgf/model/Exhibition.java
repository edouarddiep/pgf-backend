package com.pgf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

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

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExhibitionStatus status = ExhibitionStatus.UPCOMING;

    public enum ExhibitionStatus {
        UPCOMING,
        ONGOING,
        PAST
    }
}