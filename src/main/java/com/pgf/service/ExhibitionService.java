package com.pgf.service;

import com.pgf.dto.ExhibitionDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ExhibitionMapper;
import com.pgf.model.Exhibition;
import com.pgf.repository.ExhibitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionMapper exhibitionMapper;
    private final FileUploadService fileUploadService;
    private final DeepLService deepLService;

    @Cacheable("exhibitions")
    @Transactional(readOnly = true)
    public List<ExhibitionDto> findAll() {
        return toDtos(exhibitionRepository.findAllByOrderByStartDateDesc());
    }

    @Transactional(readOnly = true)
    public ExhibitionDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findUpcoming() {
        return toDtos(exhibitionRepository.findByStartDateAfterOrderByStartDateAsc(LocalDate.now()));
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findOngoing() {
        LocalDate today = LocalDate.now();
        return toDtos(exhibitionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(today, today));
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findPast() {
        return toDtos(exhibitionRepository.findByEndDateBeforeOrderByStartDateDesc(LocalDate.now()));
    }

    @CacheEvict(value = "exhibitions", allEntries = true)
    public ExhibitionDto create(ExhibitionDto exhibitionDto) {
        Exhibition exhibition = exhibitionMapper.toEntity(exhibitionDto);
        exhibition.setTitleEn(deepLService.translate(exhibition.getTitle()));
        exhibition.setDescriptionEn(deepLService.translate(exhibition.getDescription()));
        return toDto(exhibitionRepository.save(exhibition));
    }

    @CacheEvict(value = "exhibitions", allEntries = true)
    public ExhibitionDto update(Long id, ExhibitionDto exhibitionDto) {
        Exhibition exhibition = getOrThrow(id);
        String previousTitle = exhibition.getTitle();
        String previousDescription = exhibition.getDescription();

        exhibitionMapper.updateEntityFromDto(exhibitionDto, exhibition);

        exhibition.setTitleEn(deepLService.translateIfChanged(previousTitle, exhibition.getTitle(), exhibition.getTitleEn()));
        exhibition.setDescriptionEn(deepLService.translateIfChanged(previousDescription, exhibition.getDescription(), exhibition.getDescriptionEn()));

        return toDto(exhibitionRepository.save(exhibition));
    }

    @CacheEvict(value = "exhibitions", allEntries = true)
    public void delete(Long id) {
        Exhibition exhibition = getOrThrow(id);
        deleteMedia(exhibition.getImageUrls());
        deleteMedia(exhibition.getVideoUrls());
        exhibitionRepository.delete(exhibition);
    }

    private Exhibition getOrThrow(Long id) {
        return exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + id));
    }

    private void deleteMedia(List<String> urls) {
        if (urls != null) {
            urls.forEach(fileUploadService::deleteImage);
        }
    }

    private List<ExhibitionDto> toDtos(List<Exhibition> exhibitions) {
        return exhibitions.stream().map(this::toDto).toList();
    }

    private ExhibitionDto toDto(Exhibition exhibition) {
        exhibition.setStatus(resolveStatus(exhibition));
        return exhibitionMapper.toDto(exhibition);
    }

    private Exhibition.ExhibitionStatus resolveStatus(Exhibition exhibition) {
        LocalDate startDate = exhibition.getStartDate();
        LocalDate endDate = exhibition.getEndDate();
        LocalDate today = LocalDate.now();

        if (startDate == null || today.isBefore(startDate)) {
            return Exhibition.ExhibitionStatus.UPCOMING;
        }
        if (endDate != null && today.isAfter(endDate)) {
            return Exhibition.ExhibitionStatus.PAST;
        }
        return Exhibition.ExhibitionStatus.ONGOING;
    }
}
