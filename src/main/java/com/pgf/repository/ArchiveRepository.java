package com.pgf.repository;

import com.pgf.model.Archive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchiveRepository extends JpaRepository<Archive, Long> {
    List<Archive> findAllByOrderByYearDescTitleAsc();
}
