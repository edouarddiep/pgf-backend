package com.pgf.repository;

import com.pgf.model.Archive;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArchiveRepository extends BaseRepository<Archive> {
    List<Archive> findAllByOrderByYearDescDisplayOrderAsc();
}