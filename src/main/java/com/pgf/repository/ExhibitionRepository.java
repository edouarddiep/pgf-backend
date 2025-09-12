package com.pgf.repository;

import com.pgf.model.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {
    List<Exhibition> findAllByOrderByStartDateDesc();
    Optional<Exhibition> findFirstByIsFeaturedTrueAndStartDateAfterOrderByStartDateAsc(LocalDate date);
    List<Exhibition> findByEndDateBeforeOrderByStartDateDesc(LocalDate date);
    List<Exhibition> findByStatusOrderByStartDateDesc(Exhibition.ExhibitionStatus status);
    List<Exhibition> findByStartDateAfterOrderByStartDateAsc(LocalDate date);
    List<Exhibition> findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate start, LocalDate end);
}