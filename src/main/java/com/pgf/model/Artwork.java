package com.pgf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "artworks")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "categories")
@ToString(exclude = "categories")
public class Artwork extends BaseEntity {

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "description_short", length = 500)
    private String descriptionShort;

    @Column(name = "image_urls", columnDefinition = "TEXT[]")
    private List<String> imageUrls;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "artwork_categories_mapping",
            joinColumns = @JoinColumn(name = "artwork_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<ArtworkCategory> categories = new HashSet<>();
}