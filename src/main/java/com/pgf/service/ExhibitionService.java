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
        return exhibitionRepository.findAllByOrderByStartDateDesc()
                .stream()
                .map(exhibitionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ExhibitionDto> findById(Long id) {
        return exhibitionRepository.findById(id)
                .map(exhibitionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<ExhibitionDto> findFeaturedExhibition() {
        return exhibitionRepository.findFirstByIsFeaturedTrueAndStartDateAfterOrderByStartDateAsc(LocalDate.now())
                .map(exhibitionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findUpcomingExhibitions() {
        return exhibitionRepository.findByStartDateAfterOrderByStartDateAsc(LocalDate.now())
                .stream()
                .map(exhibitionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findOngoingExhibitions() {
        return exhibitionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
                        LocalDate.now(), LocalDate.now())
                .stream()
                .map(exhibitionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findPastExhibitions() {
        return exhibitionRepository.findByEndDateBeforeOrderByStartDateDesc(LocalDate.now())
                .stream()
                .map(exhibitionMapper::toDto)
                .toList();
    }

    public ExhibitionDto create(ExhibitionDto exhibitionDto) {
        Exhibition exhibition = exhibitionMapper.toEntity(exhibitionDto);
        calculateStatus(exhibition);
        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return exhibitionMapper.toDto(savedExhibition);
    }

    public ExhibitionDto update(Long id, ExhibitionDto exhibitionDto) {
        Exhibition existingExhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + id));

        exhibitionMapper.updateEntityFromDto(exhibitionDto, existingExhibition);
        calculateStatus(existingExhibition);
        Exhibition updatedExhibition = exhibitionRepository.save(existingExhibition);
        return exhibitionMapper.toDto(updatedExhibition);
    }

    public void delete(Long id) {
        if (!exhibitionRepository.existsById(id)) {
            throw new EntityNotFoundException("Exhibition not found with id: " + id);
        }
        exhibitionRepository.deleteById(id);
    }

    private void calculateStatus(Exhibition exhibition) {
        if (exhibition.getStartDate() == null || exhibition.getEndDate() == null) {
            exhibition.setStatus(Exhibition.ExhibitionStatus.UPCOMING);
            return;
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(exhibition.getStartDate())) {
            exhibition.setStatus(Exhibition.ExhibitionStatus.UPCOMING);
        } else if (today.isAfter(exhibition.getEndDate())) {
            exhibition.setStatus(Exhibition.ExhibitionStatus.PAST);
        } else {
            exhibition.setStatus(Exhibition.ExhibitionStatus.ONGOING);
        }
    }
}