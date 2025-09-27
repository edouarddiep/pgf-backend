package com.pgf.service;

import com.pgf.dto.ExhibitionDto;
import com.pgf.mapper.ExhibitionMapper;
import com.pgf.model.Exhibition;
import com.pgf.repository.ExhibitionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionMapper exhibitionMapper;

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findAll() {
        return exhibitionRepository.findAllByOrderByDisplayOrderAscStartDateDesc()
                .stream()
                .map(this::mapWithCalculatedStatus)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ExhibitionDto> findById(Long id) {
        return exhibitionRepository.findById(id)
                .map(this::mapWithCalculatedStatus);
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findUpcomingExhibitions() {
        return exhibitionRepository.findByStartDateAfterOrderByStartDateAsc(LocalDate.now())
                .stream()
                .map(this::mapWithCalculatedStatus)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findOngoingExhibitions() {
        return exhibitionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
                        LocalDate.now(), LocalDate.now())
                .stream()
                .map(this::mapWithCalculatedStatus)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findPastExhibitions() {
        return exhibitionRepository.findByEndDateBeforeOrderByStartDateDesc(LocalDate.now())
                .stream()
                .map(this::mapWithCalculatedStatus)
                .toList();
    }

    public ExhibitionDto create(ExhibitionDto exhibitionDto) {
        Exhibition exhibition = exhibitionMapper.toEntity(exhibitionDto);
        calculateAndSetStatus(exhibition);

        if (exhibition.getDisplayOrder() == null) {
            exhibition.setDisplayOrder(getNextDisplayOrder());
        }

        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return mapWithCalculatedStatus(savedExhibition);
    }

    public ExhibitionDto update(Long id, ExhibitionDto exhibitionDto) {
        Exhibition existingExhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + id));

        exhibitionMapper.updateEntityFromDto(exhibitionDto, existingExhibition);
        calculateAndSetStatus(existingExhibition);
        Exhibition updatedExhibition = exhibitionRepository.save(existingExhibition);
        return mapWithCalculatedStatus(updatedExhibition);
    }

    public void delete(Long id) {
        if (!exhibitionRepository.existsById(id)) {
            throw new EntityNotFoundException("Exhibition not found with id: " + id);
        }
        exhibitionRepository.deleteById(id);
    }

    public void updateDisplayOrder(Long id, Integer newOrder) {
        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + id));
        exhibition.setDisplayOrder(newOrder);
        exhibitionRepository.save(exhibition);
    }

    private ExhibitionDto mapWithCalculatedStatus(Exhibition exhibition) {
        calculateAndSetStatus(exhibition);
        return exhibitionMapper.toDto(exhibition);
    }

    private void calculateAndSetStatus(Exhibition exhibition) {
        if (exhibition.getStartDate() == null) {
            exhibition.setStatus(Exhibition.ExhibitionStatus.UPCOMING);
            return;
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(exhibition.getStartDate())) {
            exhibition.setStatus(Exhibition.ExhibitionStatus.UPCOMING);
        } else if (exhibition.getEndDate() != null && today.isAfter(exhibition.getEndDate())) {
            exhibition.setStatus(Exhibition.ExhibitionStatus.PAST);
        } else {
            exhibition.setStatus(Exhibition.ExhibitionStatus.ONGOING);
        }
    }

    private Integer getNextDisplayOrder() {
        return exhibitionRepository.findMaxDisplayOrder().orElse(0) + 1;
    }
}