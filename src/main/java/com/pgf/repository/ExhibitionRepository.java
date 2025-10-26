package com.pgf.repository;

import com.pgf.model.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    List<Exhibition> findAllByOrderByDisplayOrderAscStartDateDesc();

    List<Exhibition> findByEndDateBeforeOrderByStartDateDesc(LocalDate date);

    List<Exhibition> findByStartDateAfterOrderByStartDateAsc(LocalDate date);

    List<Exhibition> findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate start, LocalDate end);

    List<Exhibition> findByStatusOrderByStartDateAsc(Exhibition.ExhibitionStatus status);

    List<Exhibition> findByStatusOrderByStartDateDesc(Exhibition.ExhibitionStatus status);

    @Query("SELECT COALESCE(MAX(e.displayOrder), 0) FROM Exhibition e")
    Optional<Integer> findMaxDisplayOrder();
}