package com.pgf.service;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.exception.ConflictException;
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

@Service
@Transactional
@RequiredArgsConstructor
public class ArtworkCategoryService {

    private final ArtworkCategoryRepository categoryRepository;
    private final ArtworkRepository artworkRepository;
    private final ArtworkCategoryMapper categoryMapper;
    private final FileUploadService fileUploadService;
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
        return categoryMapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ArtworkCategoryDto findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with slug: " + slug));
    }

    @CacheEvict(value = {"categories", "sitemap"}, allEntries = true)
    public ArtworkCategoryDto create(ArtworkCategoryDto categoryDto) {
        if (categoryRepository.existsBySlug(categoryDto.getSlug())) {
            throw new ConflictException("Une catégorie avec ce slug existe déjà : " + categoryDto.getSlug());
        }
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Une catégorie avec ce nom existe déjà : " + categoryDto.getName());
        }
        ArtworkCategory category = categoryMapper.toEntity(categoryDto);
        category.setNameEn(deepLService.translate(category.getName()));
        category.setDescriptionEn(deepLService.translate(category.getDescription()));
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @CacheEvict(value = {"categories", "sitemap"}, allEntries = true)
    public ArtworkCategoryDto update(Long id, ArtworkCategoryDto categoryDto) {
        ArtworkCategory category = getOrThrow(id);
        String previousName = category.getName();
        String previousDescription = category.getDescription();

        categoryMapper.updateEntityFromDto(categoryDto, category);

        category.setNameEn(deepLService.translateIfChanged(previousName, category.getName(), category.getNameEn()));
        category.setDescriptionEn(deepLService.translateIfChanged(previousDescription, category.getDescription(), category.getDescriptionEn()));

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @CacheEvict(value = {"categories", "sitemap"}, allEntries = true)
    public void delete(Long id) {
        ArtworkCategory category = getOrThrow(id);
        long artworkCount = artworkRepository.countByCategoriesContaining(category);
        if (artworkCount > 0) {
            throw new ConflictException("Impossible de supprimer une catégorie contenant des œuvres (" + artworkCount + " œuvre(s) liée(s))");
        }
        if (category.getThumbnailUrl() != null) {
            fileUploadService.deleteImage(category.getThumbnailUrl());
        }
        categoryRepository.delete(category);
    }

    private ArtworkCategory getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }
}
