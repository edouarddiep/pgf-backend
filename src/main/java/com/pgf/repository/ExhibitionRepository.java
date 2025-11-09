package com.pgf.repository;

import com.pgf.model.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    List<Exhibition> findAllByOrderByStartDateDesc();

    List<Exhibition> findByEndDateBeforeOrderByStartDateDesc(LocalDate date);

    List<Exhibition> findByStartDateAfterOrderByStartDateAsc(LocalDate date);

    List<Exhibition> findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate start, LocalDate end);

    List<Exhibition> findByStatusOrderByStartDateAsc(Exhibition.ExhibitionStatus status);

    List<Exhibition> findByStatusOrderByStartDateDesc(Exhibition.ExhibitionStatus status);
}