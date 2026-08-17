package com.pgf.repository;

import com.pgf.model.Exhibition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    @EntityGraph(attributePaths = "files")
    List<Exhibition> findAllByOrderByStartDateDesc();

    @EntityGraph(attributePaths = "files")
    List<Exhibition> findByEndDateBeforeOrderByStartDateDesc(LocalDate date);

    @EntityGraph(attributePaths = "files")
    List<Exhibition> findByStartDateAfterOrderByStartDateAsc(LocalDate date);

    @EntityGraph(attributePaths = "files")
    List<Exhibition> findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = "files")
    Optional<Exhibition> findWithFilesById(Long id);
}
