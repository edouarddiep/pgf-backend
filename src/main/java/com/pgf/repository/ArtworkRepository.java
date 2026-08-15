package com.pgf.repository;

import com.pgf.model.Artwork;
import com.pgf.model.ArtworkCategory;
import com.pgf.repository.projection.ArtworkSitemapView;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    @EntityGraph(attributePaths = "categories")
    List<Artwork> findAllByOrderByTitleAsc();

    @Query("SELECT DISTINCT a FROM Artwork a LEFT JOIN FETCH a.categories WHERE a.id = :id")
    Optional<Artwork> findByIdWithCategories(@Param("id") Long id);

    @EntityGraph(attributePaths = "categories")
    @Query("""
            SELECT a FROM Artwork a
            WHERE EXISTS (SELECT 1 FROM a.categories c WHERE c.id = :categoryId)
            ORDER BY a.title ASC
            """)
    List<Artwork> findByCategoryId(@Param("categoryId") Long categoryId);

    @EntityGraph(attributePaths = "categories")
    @Query("""
            SELECT a FROM Artwork a
            WHERE EXISTS (SELECT 1 FROM a.categories c WHERE c.slug = :slug)
            ORDER BY a.title ASC
            """)
    List<Artwork> findByCategorySlug(@Param("slug") String slug);

    @Query("SELECT COUNT(a) FROM Artwork a JOIN a.categories c WHERE c = :category")
    long countByCategoriesContaining(@Param("category") ArtworkCategory category);

    @Query("""
            SELECT a.id AS id, MIN(c.slug) AS categorySlug
            FROM Artwork a JOIN a.categories c
            GROUP BY a.id
            ORDER BY a.id ASC
            """)
    List<ArtworkSitemapView> findSitemapEntries();
}
