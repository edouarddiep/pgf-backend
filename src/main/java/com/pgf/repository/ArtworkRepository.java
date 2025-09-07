package com.pgf.repository;

import com.pgf.model.Artwork;
import com.pgf.model.ArtworkCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    List<Artwork> findByCategoryOrderByDisplayOrderAscCreatedAtDesc(ArtworkCategory category);

    List<Artwork> findByCategoryIdOrderByDisplayOrderAscCreatedAtDesc(Long categoryId);

    List<Artwork> findByIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();

    @Query("SELECT a FROM Artwork a WHERE a.category.slug = :categorySlug ORDER BY a.displayOrder ASC, a.createdAt DESC")
    List<Artwork> findByCategorySlugOrderByDisplayOrder(@Param("categorySlug") String categorySlug);

    long countByCategory(ArtworkCategory category);
}