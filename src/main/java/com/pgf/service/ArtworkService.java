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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkCategoryRepository categoryRepository;
    private final ArtworkMapper artworkMapper;

    @Transactional(readOnly = true)
    public List<ArtworkDto> findAll() {
        return artworkRepository.findAll(Sort.by("displayOrder").ascending())
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtworkDto findById(Long id) {
        System.out.println("DEBUG - Finding artwork with id: " + id);
        return artworkRepository.findByIdWithCategories(id)
                .map(artwork -> {
                    System.out.println("DEBUG - Found artwork: " + artwork.getTitle());
                    System.out.println("DEBUG - Categories in entity: " + artwork.getCategories().size());
                    return artworkMapper.toDto(artwork);
                })
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findByCategoryId(Long categoryId) {
        return artworkRepository.findByCategoriesIdIn(Set.of(categoryId))
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

    @Transactional(readOnly = true)
    public List<ArtworkDto> findByCategoriesIdIn(Set<Long> categoryIds) {
        return artworkRepository.findByCategoriesIdIn(categoryIds)
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findAvailableArtworks() {
        return artworkRepository.findByIsAvailableTrueOrderByDisplayOrderAsc()
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    public ArtworkDto create(ArtworkDto artworkDto) {
        Artwork artwork = artworkMapper.toEntity(artworkDto);

        // Gestion explicite des catégories
        if (artworkDto.getCategoryIds() != null && !artworkDto.getCategoryIds().isEmpty()) {
            Set<ArtworkCategory> categories = new HashSet<>(categoryRepository.findAllById(artworkDto.getCategoryIds()));
            if (categories.isEmpty()) {
                throw new IllegalArgumentException("Aucune catégorie valide trouvée avec les IDs fournis");
            }
            artwork.setCategories(categories);
        } else {
            throw new IllegalArgumentException("Au moins une catégorie doit être spécifiée");
        }

        Artwork savedArtwork = artworkRepository.save(artwork);
        log.info("Created artwork: {} with {} categories", savedArtwork.getTitle(), savedArtwork.getCategories().size());

        return artworkMapper.toDto(savedArtwork);
    }

    public ArtworkDto update(Long id, ArtworkDto artworkDto) {
        Artwork existingArtwork = artworkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found with id: " + id));

        // Mise à jour des champs de base
        artworkMapper.updateEntityFromDto(artworkDto, existingArtwork);

        // Gestion explicite des catégories
        if (artworkDto.getCategoryIds() != null) {
            // Vider les catégories existantes
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

        Artwork savedArtwork = artworkRepository.save(artwork);
        return artworkMapper.toDto(savedArtwork);
    }

    public void delete(Long id) {
        if (!artworkRepository.existsById(id)) {
            throw new EntityNotFoundException("Artwork not found with id: " + id);
        }
        artworkRepository.deleteById(id);
    }
}