package com.pgf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "artwork_categories")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "artworks")
@ToString(exclude = "artworks")
public class ArtworkCategory extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<Artwork> artworks = new HashSet<>();
}