package com.pgf.service;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ArtworkCategoryMapper;
import com.pgf.model.ArtworkCategory;
import com.pgf.repository.ArtworkCategoryRepository;
import com.pgf.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtworkCategoryService {

    private final ArtworkCategoryRepository categoryRepository;
    private final ArtworkRepository artworkRepository;
    private final ArtworkCategoryMapper categoryMapper;
    private final FileUploadService imageService;
    private final DeepLService deepLService;

    @Cacheable("categories")
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

    @CacheEvict(value = "categories", allEntries = true)
    public ArtworkCategoryDto create(ArtworkCategoryDto categoryDto) {
        if (categoryRepository.existsBySlug(categoryDto.getSlug())) {
            throw new IllegalArgumentException("Category with slug already exists: " + categoryDto.getSlug());
        }
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name already exists: " + categoryDto.getName());
        }
        ArtworkCategory category = categoryMapper.toEntity(categoryDto);
        translateAllFields(category);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public ArtworkCategoryDto update(Long id, ArtworkCategoryDto categoryDto) {
        ArtworkCategory existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        String previousName = existingCategory.getName();
        String previousDescription = existingCategory.getDescription();
        String existingNameEn = existingCategory.getNameEn();
        String existingDescriptionEn = existingCategory.getDescriptionEn();

        categoryMapper.updateEntityFromDto(categoryDto, existingCategory);

        if (existingCategory.getNameEn() == null) {
            existingCategory.setNameEn(existingNameEn);
        }
        if (existingCategory.getDescriptionEn() == null) {
            existingCategory.setDescriptionEn(existingDescriptionEn);
        }

        translateChangedFields(previousName, previousDescription, existingCategory);

        return categoryMapper.toDto(categoryRepository.save(existingCategory));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id) {
        ArtworkCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        long artworkCount = artworkRepository.countByCategoriesContaining(category);
        if (artworkCount > 0) {
            throw new IllegalStateException("Impossible de supprimer une catégorie contenant des œuvres (" + artworkCount + " œuvre(s) liée(s))");
        }
        if (category.getThumbnailUrl() != null) {
            imageService.deleteImage(category.getThumbnailUrl());
        }
        categoryRepository.deleteById(id);
    }

    private void translateAllFields(ArtworkCategory category) {
        category.setNameEn(deepLService.translate(category.getName()));
        category.setDescriptionEn(deepLService.translate(category.getDescription()));
    }

    private void translateChangedFields(String previousName, String previousDescription, ArtworkCategory category) {
        if (!Objects.equals(previousName, category.getName())) {
            category.setNameEn(deepLService.translate(category.getName()));
        }
        if (!Objects.equals(previousDescription, category.getDescription())) {
            category.setDescriptionEn(deepLService.translate(category.getDescription()));
        }
    }
}