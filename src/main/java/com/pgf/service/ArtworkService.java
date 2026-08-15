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
import org.springframework.util.CollectionUtils;

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
    private final FileUploadService fileUploadService;
    private final DeepLService deepLService;

    @Cacheable("artworks")
    @Transactional(readOnly = true)
    public List<ArtworkDto> findAll() {
        return toDtos(artworkRepository.findAll(Sort.by("title").ascending()));
    }

    @Transactional(readOnly = true)
    public ArtworkDto findById(Long id) {
        return artworkRepository.findByIdWithCategories(id)
                .map(artworkMapper::toDto)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findByCategoryId(Long categoryId) {
        return toDtos(artworkRepository.findByCategoryId(categoryId));
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findByCategorySlug(String categorySlug) {
        return toDtos(artworkRepository.findByCategorySlug(categorySlug));
    }

    @CacheEvict(value = {"artworks", "sitemap"}, allEntries = true)
    public ArtworkDto create(ArtworkDto artworkDto) {
        Artwork artwork = artworkMapper.toEntity(artworkDto);
        artwork.setCategories(resolveCategories(artworkDto.getCategoryIds()));
        artwork.setTitleEn(deepLService.translate(artwork.getTitle()));
        artwork.setDescriptionEn(deepLService.translate(artwork.getDescription()));

        Artwork saved = artworkRepository.save(artwork);
        log.info("Created artwork: {} with {} categories", saved.getTitle(), saved.getCategories().size());
        return artworkMapper.toDto(saved);
    }

    @CacheEvict(value = {"artworks", "sitemap"}, allEntries = true)
    public ArtworkDto update(Long id, ArtworkDto artworkDto) {
        Artwork artwork = getOrThrow(id);
        String previousTitle = artwork.getTitle();
        String previousDescription = artwork.getDescription();

        artworkMapper.updateEntityFromDto(artworkDto, artwork);

        if (artworkDto.getCategoryIds() != null) {
            replaceCategories(artwork, resolveCategories(artworkDto.getCategoryIds()));
        }

        artwork.setTitleEn(deepLService.translateIfChanged(previousTitle, artwork.getTitle(), artwork.getTitleEn()));
        artwork.setDescriptionEn(deepLService.translateIfChanged(previousDescription, artwork.getDescription(), artwork.getDescriptionEn()));

        Artwork saved = artworkRepository.save(artwork);
        log.info("Updated artwork: {} with {} categories", saved.getTitle(), saved.getCategories().size());
        return artworkMapper.toDto(saved);
    }

    @CacheEvict(value = {"artworks", "sitemap"}, allEntries = true)
    public void delete(Long id) {
        Artwork artwork = getOrThrow(id);
        if (artwork.getImageUrls() != null) {
            artwork.getImageUrls().forEach(fileUploadService::deleteImage);
        }
        artworkRepository.delete(artwork);
    }

    private Artwork getOrThrow(Long id) {
        return artworkRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private EntityNotFoundException notFound(Long id) {
        return new EntityNotFoundException("Artwork not found with id: " + id);
    }

    private Set<ArtworkCategory> resolveCategories(Set<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            throw new IllegalArgumentException("Au moins une catégorie doit être spécifiée");
        }
        Set<ArtworkCategory> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("Aucune catégorie valide trouvée avec les IDs fournis");
        }
        return categories;
    }

    private void replaceCategories(Artwork artwork, Set<ArtworkCategory> categories) {
        artwork.getCategories().clear();
        artwork.getCategories().addAll(categories);
    }

    private List<ArtworkDto> toDtos(List<Artwork> artworks) {
        return artworks.stream().map(artworkMapper::toDto).toList();
    }
}
