package com.pgf.repository;

import com.pgf.model.ExhibitionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExhibitionFileRepository extends JpaRepository<ExhibitionFile, Long> {

    List<ExhibitionFile> findByExhibitionIdOrderByDisplayOrderAscIdAsc(Long exhibitionId);

    List<ExhibitionFile> findByExhibitionIdAndMediaTypeOrderByDisplayOrderAscIdAsc(Long exhibitionId, ExhibitionFile.MediaType mediaType);

    Optional<ExhibitionFile> findByIdAndExhibitionId(Long id, Long exhibitionId);

    @Query("SELECT COALESCE(MAX(file.displayOrder), -1) + 1 FROM ExhibitionFile file WHERE file.exhibition.id = :exhibitionId")
    int nextDisplayOrder(@Param("exhibitionId") Long exhibitionId);
}
