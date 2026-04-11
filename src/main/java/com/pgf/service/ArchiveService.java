package com.pgf.service;

import com.pgf.dto.ArchiveDto;
import com.pgf.mapper.ArchiveMapper;
import com.pgf.model.Archive;
import com.pgf.repository.ArchiveRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final ArchiveMapper archiveMapper;

    @Cacheable("archives")
    @Transactional(readOnly = true)
    public List<ArchiveDto> findAll() {
        return archiveRepository.findAllByOrderByYearDescTitleAsc()
                .stream()
                .map(archiveMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ArchiveDto> findById(Long id) {
        return archiveRepository.findById(id)
                .map(archiveMapper::toDto);
    }

    @CacheEvict(value = "archives", allEntries = true)
    public ArchiveDto create(ArchiveDto archiveDto) {
        Archive archive = archiveMapper.toEntity(archiveDto);
        if (archive.getFiles() != null) {
            archive.getFiles().forEach(f -> f.setArchive(archive));
        }
        return archiveMapper.toDto(archiveRepository.save(archive));
    }

    @CacheEvict(value = "archives", allEntries = true)
    public ArchiveDto update(Long id, ArchiveDto archiveDto) {
        Archive existingArchive = archiveRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Archive not found with id: " + id));
        archiveMapper.updateEntityFromDto(archiveDto, existingArchive);
        if (existingArchive.getFiles() != null) {
            existingArchive.getFiles().forEach(f -> f.setArchive(existingArchive));
        }
        return archiveMapper.toDto(archiveRepository.save(existingArchive));
    }

    @CacheEvict(value = "archives", allEntries = true)
    public void delete(Long id) {
        if (!archiveRepository.existsById(id)) {
            throw new EntityNotFoundException("Archive not found with id: " + id);
        }
        archiveRepository.deleteById(id);
    }
}