package com.pgf.service;

import com.pgf.dto.ArtworkDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ArtworkMapper;
import com.pgf.model.Artwork;
import com.pgf.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkMapper artworkMapper;

    @Transactional(readOnly = true)
    public List<ArtworkDto> findAll() {
        return artworkRepository.findAll()
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtworkDto findById(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found with id: " + id));
        return artworkMapper.toDto(artwork);
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findByCategoryId(Long categoryId) {
        return artworkRepository.findByCategoryIdOrderByDisplayOrderAscCreatedAtDesc(categoryId)
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findByCategorySlug(String categorySlug) {
        return artworkRepository.findByCategorySlugOrderByDisplayOrder(categorySlug)
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArtworkDto> findAvailableArtworks() {
        return artworkRepository.findByIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc()
                .stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    public ArtworkDto create(ArtworkDto artworkDto) {
        Artwork artwork = artworkMapper.toEntity(artworkDto);
        Artwork savedArtwork = artworkRepository.save(artwork);
        return artworkMapper.toDto(savedArtwork);
    }

    public ArtworkDto update(Long id, ArtworkDto artworkDto) {
        Artwork existingArtwork = artworkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found with id: " + id));

        artworkMapper.updateEntityFromDto(artworkDto, existingArtwork);
        Artwork updatedArtwork = artworkRepository.save(existingArtwork);
        return artworkMapper.toDto(updatedArtwork);
    }

    public void delete(Long id) {
        if (!artworkRepository.existsById(id)) {
            throw new EntityNotFoundException("Artwork not found with id: " + id);
        }
        artworkRepository.deleteById(id);
    }
}