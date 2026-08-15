package com.pgf.service;

import com.pgf.config.CacheConfig;
import com.pgf.dto.ArchiveDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ArchiveMapper;
import com.pgf.model.Archive;
import com.pgf.repository.ArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final ArchiveMapper archiveMapper;
    private final DeepLService deepLService;

    @Cacheable(CacheConfig.ARCHIVES)
    @Transactional(readOnly = true)
    public List<ArchiveDto> findAll() {
        return archiveRepository.findAllByOrderByYearDescTitleAsc()
                .stream()
                .map(archiveMapper::toDto)
                .toList();
    }

    @Cacheable(value = CacheConfig.ARCHIVE, key = "#id")
    @Transactional(readOnly = true)
    public ArchiveDto findById(Long id) {
        return archiveRepository.findWithFilesById(id)
                .map(archiveMapper::toDto)
                .orElseThrow(() -> notFound(id));
    }

    @CacheEvict(value = {CacheConfig.ARCHIVES, CacheConfig.ARCHIVE}, allEntries = true)
    public ArchiveDto create(ArchiveDto archiveDto) {
        Archive archive = archiveMapper.toEntity(archiveDto);
        linkFiles(archive);
        archive.setTitleEn(deepLService.translate(archive.getTitle()));
        archive.setDescriptionEn(deepLService.translate(archive.getDescription()));
        return archiveMapper.toDto(archiveRepository.save(archive));
    }

    @CacheEvict(value = {CacheConfig.ARCHIVES, CacheConfig.ARCHIVE}, allEntries = true)
    public ArchiveDto update(Long id, ArchiveDto archiveDto) {
        Archive archive = getOrThrow(id);
        String previousTitle = archive.getTitle();
        String previousDescription = archive.getDescription();

        archiveMapper.updateEntityFromDto(archiveDto, archive);
        linkFiles(archive);

        archive.setTitleEn(deepLService.translateIfChanged(previousTitle, archive.getTitle(), archive.getTitleEn()));
        archive.setDescriptionEn(deepLService.translateIfChanged(previousDescription, archive.getDescription(), archive.getDescriptionEn()));

        return archiveMapper.toDto(archiveRepository.save(archive));
    }

    @CacheEvict(value = {CacheConfig.ARCHIVES, CacheConfig.ARCHIVE}, allEntries = true)
    public void delete(Long id) {
        archiveRepository.delete(getOrThrow(id));
    }

    private Archive getOrThrow(Long id) {
        return archiveRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private EntityNotFoundException notFound(Long id) {
        return new EntityNotFoundException("Archive not found with id: " + id);
    }

    private void linkFiles(Archive archive) {
        if (archive.getFiles() != null) {
            archive.getFiles().forEach(file -> file.setArchive(archive));
        }
    }
}
