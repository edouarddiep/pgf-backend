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

    List<Exhibition> findByStatusOrderByStartDateDesc(Exhibition.ExhibitionStatus status);

    @Query("SELECT e FROM Exhibition e WHERE e.status = 'UPCOMING' ORDER BY e.startDate ASC")
    List<Exhibition> findUpcomingExhibitions();

    @Query("SELECT e FROM Exhibition e WHERE e.status = 'PAST' ORDER BY e.startDate DESC")
    List<Exhibition> findPastExhibitions();

    @Query("SELECT e FROM Exhibition e WHERE e.isFeatured = true AND e.status = 'UPCOMING' ORDER BY e.startDate ASC")
    Optional<Exhibition> findNextFeaturedExhibition();

    @Query("SELECT e FROM Exhibition e WHERE e.startDate <= :today AND e.endDate >= :today")
    List<Exhibition> findOngoingExhibitions(LocalDate today);

    List<Exhibition> findByIsFeaturedTrueOrderByStartDateAsc();
}