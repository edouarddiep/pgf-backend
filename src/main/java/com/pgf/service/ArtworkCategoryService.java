package com.pgf.service;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ArtworkCategoryMapper;
import com.pgf.model.ArtworkCategory;
import com.pgf.repository.ArtworkCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtworkCategoryService {

    private final ArtworkCategoryRepository categoryRepository;
    private final ArtworkCategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<ArtworkCategoryDto> findAll() {
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtworkCategoryDto findById(Long id) {
        ArtworkCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Transactional(readOnly = true)
    public ArtworkCategoryDto findBySlug(String slug) {
        ArtworkCategory category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with slug: " + slug));
        return categoryMapper.toDto(category);
    }

    public ArtworkCategoryDto create(ArtworkCategoryDto categoryDto) {
        if (categoryRepository.existsBySlug(categoryDto.getSlug())) {
            throw new IllegalArgumentException("Category with slug already exists: " + categoryDto.getSlug());
        }
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name already exists: " + categoryDto.getName());
        }

        ArtworkCategory category = categoryMapper.toEntity(categoryDto);
        ArtworkCategory savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    public ArtworkCategoryDto update(Long id, ArtworkCategoryDto categoryDto) {
        ArtworkCategory existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        categoryMapper.updateEntityFromDto(categoryDto, existingCategory);
        ArtworkCategory updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}