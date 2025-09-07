package com.pgf.repository;

import com.pgf.model.ArtworkCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworkCategoryRepository extends JpaRepository<ArtworkCategory, Long> {

    Optional<ArtworkCategory> findBySlug(String slug);

    List<ArtworkCategory> findAllByOrderByDisplayOrderAscNameAsc();

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}