package com.pgf.repository;

import com.pgf.model.Archive;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    @EntityGraph(attributePaths = "files")
    List<Archive> findAllByOrderByYearDescTitleAsc();

    @EntityGraph(attributePaths = "files")
    Optional<Archive> findWithFilesById(Long id);
}
