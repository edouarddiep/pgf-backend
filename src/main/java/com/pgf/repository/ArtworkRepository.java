package com.pgf.repository;

import com.pgf.model.Artwork;
import com.pgf.model.ArtworkCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    @Query("SELECT DISTINCT a FROM Artwork a LEFT JOIN FETCH a.categories WHERE a.id = :id")
    Optional<Artwork> findByIdWithCategories(@Param("id") Long id);

    @Query("SELECT a FROM Artwork a JOIN a.categories c WHERE c.id IN :categoryIds")
    List<Artwork> findByCategoriesIdIn(@Param("categoryIds") Set<Long> categoryIds);

    @Query("SELECT a FROM Artwork a JOIN a.categories c WHERE c.slug = :slug")
    List<Artwork> findByCategorySlug(@Param("slug") String slug);

    @Query("SELECT COUNT(a) FROM Artwork a JOIN a.categories c WHERE c = :category")
    long countByCategoriesContaining(@Param("category") ArtworkCategory category);
}