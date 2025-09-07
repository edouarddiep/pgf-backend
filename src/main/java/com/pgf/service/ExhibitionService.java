package com.pgf.service;

import com.pgf.dto.ExhibitionDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ExhibitionMapper;
import com.pgf.model.Exhibition;
import com.pgf.repository.ExhibitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionMapper exhibitionMapper;

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findAll() {
        return exhibitionRepository.findAll()
                .stream()
                .map(exhibitionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExhibitionDto findById(Long id) {
        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + id));
        return exhibitionMapper.toDto(exhibition);
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findUpcoming() {
        return exhibitionRepository.findUpcomingExhibitions()
                .stream()
                .map(exhibitionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findPast() {
        return exhibitionRepository.findPastExhibitions()
                .stream()
                .map(exhibitionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ExhibitionDto> findNextFeatured() {
        return exhibitionRepository.findNextFeaturedExhibition()
                .map(exhibitionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ExhibitionDto> findOngoing() {
        return exhibitionRepository.findOngoingExhibitions(LocalDate.now())
                .stream()
                .map(exhibitionMapper::toDto)
                .toList();
    }

    public ExhibitionDto create(ExhibitionDto exhibitionDto) {
        Exhibition exhibition = exhibitionMapper.toEntity(exhibitionDto);
        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return exhibitionMapper.toDto(savedExhibition);
    }

    public ExhibitionDto update(Long id, ExhibitionDto exhibitionDto) {
        Exhibition existingExhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + id));

        exhibitionMapper.updateEntityFromDto(exhibitionDto, existingExhibition);
        Exhibition updatedExhibition = exhibitionRepository.save(existingExhibition);
        return exhibitionMapper.toDto(updatedExhibition);
    }

    public void delete(Long id) {
        if (!exhibitionRepository.existsById(id)) {
            throw new EntityNotFoundException("Exhibition not found with id: " + id);
        }
        exhibitionRepository.deleteById(id);
    }
}