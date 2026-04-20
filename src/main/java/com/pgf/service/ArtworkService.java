package com.pgf.service;

import com.pgf.dto.ArtworkDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ArtworkMapper;
import com.pgf.model.Artwork;
import com.pgf.model.ArtworkCategory;
import com.pgf.repository.ArtworkCategoryRepository;
import com.pgf.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkCategoryRepository categoryRepository;
    private final ArtworkMapper artworkMapper;
    private final FileUploadService imageService;
    private final DeepLService deepLService;

    @Cacheable("artworks")
    @Transactional(readOnly = true)
    public List<ArtworkDto> findAll() {
        return artworkRepository.findAll(Sort.by("title").ascending())
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtworkDto findById(Long id) {
        return artworkRepository.findByIdWithCategories(id)
                .map(artworkMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findByCategoryId(Long categoryId) {
        return artworkRepository.findByCategoryId(categoryId)
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findByCategorySlug(String categorySlug) {
        return artworkRepository.findByCategorySlug(categorySlug)
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    @CacheEvict(value = "artworks", allEntries = true)
    public ArtworkDto create(ArtworkDto artworkDto) {
        Artwork artwork = artworkMapper.toEntity(artworkDto);
        if (artworkDto.getCategoryIds() != null && !artworkDto.getCategoryIds().isEmpty()) {
            Set<ArtworkCategory> categories = new HashSet<>(categoryRepository.findAllById(artworkDto.getCategoryIds()));
            if (categories.isEmpty()) {
                throw new IllegalArgumentException("Aucune catégorie valide trouvée avec les IDs fournis");
            }
            artwork.setCategories(categories);
        } else {
            throw new IllegalArgumentException("Au moins une catégorie doit être spécifiée");
        }
        translateAllFields(artwork);
        Artwork savedArtwork = artworkRepository.save(artwork);
        log.info("Created artwork: {} with {} categories", savedArtwork.getTitle(), savedArtwork.getCategories().size());
        return artworkMapper.toDto(savedArtwork);
    }

    @CacheEvict(value = "artworks", allEntries = true)
    public ArtworkDto update(Long id, ArtworkDto artworkDto) {
        Artwork existingArtwork = artworkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found with id: " + id));

        String previousTitle = existingArtwork.getTitle();
        String previousDescription = existingArtwork.getDescription();

        artworkMapper.updateEntityFromDto(artworkDto, existingArtwork);

        if (artworkDto.getCategoryIds() != null) {
            existingArtwork.getCategories().clear();
            if (!artworkDto.getCategoryIds().isEmpty()) {
                Set<ArtworkCategory> newCategories = new HashSet<>(categoryRepository.findAllById(artworkDto.getCategoryIds()));
                if (newCategories.isEmpty()) {
                    throw new IllegalArgumentException("Aucune catégorie valide trouvée avec les IDs fournis");
                }
                existingArtwork.setCategories(newCategories);
            } else {
                throw new IllegalArgumentException("Au moins une catégorie doit être spécifiée");
            }
        }

        translateChangedFields(previousTitle, previousDescription, existingArtwork);

        Artwork updatedArtwork = artworkRepository.save(existingArtwork);
        log.info("Updated artwork: {} with {} categories", updatedArtwork.getTitle(), updatedArtwork.getCategories().size());
        return artworkMapper.toDto(updatedArtwork);
    }

    public ArtworkDto updateArtworkCategories(Long artworkId, Set<Long> categoryIds) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found with id: " + artworkId));
        artwork.getCategories().clear();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<ArtworkCategory> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
            artwork.setCategories(categories);
        }
        return artworkMapper.toDto(artworkRepository.save(artwork));
    }

    @CacheEvict(value = "artworks", allEntries = true)
    public void delete(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found with id: " + id));
        if (artwork.getImageUrls() != null) {
            artwork.getImageUrls().forEach(imageService::deleteImage);
        }
        artworkRepository.deleteById(id);
    }

    private void translateAllFields(Artwork artwork) {
        artwork.setTitleEn(deepLService.translate(artwork.getTitle()));
        artwork.setDescriptionEn(deepLService.translate(artwork.getDescription()));
    }

    private void translateChangedFields(String previousTitle, String previousDescription, Artwork artwork) {
        if (!Objects.equals(previousTitle, artwork.getTitle())) {
            artwork.setTitleEn(deepLService.translate(artwork.getTitle()));
        }
        if (!Objects.equals(previousDescription, artwork.getDescription())) {
            artwork.setDescriptionEn(deepLService.translate(artwork.getDescription()));
        }
    }
}