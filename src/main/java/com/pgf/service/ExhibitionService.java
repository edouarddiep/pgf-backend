package com.pgf.service;

import com.pgf.dto.ExhibitionDto;
import com.pgf.mapper.ExhibitionMapper;
import com.pgf.model.Exhibition;
import com.pgf.repository.ExhibitionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionMapper exhibitionMapper;
    private final FileUploadService imageService;
    private final DeepLService deepLService;

    @Cacheable("exhibitions")
    @Transactional(readOnly = true)
    public List<ExhibitionDto> findAll() {
        return exhibitionRepository.findAllByOrderByStartDateDesc()
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

    @CacheEvict(value = "exhibitions", allEntries = true)
    public ExhibitionDto create(ExhibitionDto exhibitionDto) {
        Exhibition exhibition = exhibitionMapper.toEntity(exhibitionDto);
        calculateAndSetStatus(exhibition);
        translateAllFields(exhibition);
        return mapWithCalculatedStatus(exhibitionRepository.save(exhibition));
    }

    @CacheEvict(value = "exhibitions", allEntries = true)
    public ExhibitionDto update(Long id, ExhibitionDto exhibitionDto) {
        Exhibition existingExhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + id));

        String previousTitle = existingExhibition.getTitle();
        String previousDescription = existingExhibition.getDescription();

        exhibitionMapper.updateEntityFromDto(exhibitionDto, existingExhibition);
        calculateAndSetStatus(existingExhibition);
        translateChangedFields(previousTitle, previousDescription, existingExhibition);

        return mapWithCalculatedStatus(exhibitionRepository.save(existingExhibition));
    }

    @CacheEvict(value = "exhibitions", allEntries = true)
    public void delete(Long id) {
        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + id));
        if (exhibition.getImageUrls() != null) {
            exhibition.getImageUrls().forEach(imageService::deleteImage);
        }
        if (exhibition.getVideoUrls() != null) {
            exhibition.getVideoUrls().forEach(imageService::deleteImage);
        }
        exhibitionRepository.deleteById(id);
    }

    private void translateAllFields(Exhibition exhibition) {
        exhibition.setTitleEn(deepLService.translate(exhibition.getTitle()));
        exhibition.setDescriptionEn(deepLService.translate(exhibition.getDescription()));
    }

    private void translateChangedFields(String previousTitle, String previousDescription, Exhibition exhibition) {
        if (!Objects.equals(previousTitle, exhibition.getTitle())) {
            exhibition.setTitleEn(deepLService.translate(exhibition.getTitle()));
        }
        if (!Objects.equals(previousDescription, exhibition.getDescription())) {
            exhibition.setDescriptionEn(deepLService.translate(exhibition.getDescription()));
        }
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
}