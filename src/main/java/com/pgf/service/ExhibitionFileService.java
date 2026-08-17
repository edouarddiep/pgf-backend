package com.pgf.service;

import com.pgf.config.CacheConfig;
import com.pgf.dto.ExhibitionFileDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ExhibitionFileMapper;
import com.pgf.model.Exhibition;
import com.pgf.model.ExhibitionFile;
import com.pgf.repository.ExhibitionFileRepository;
import com.pgf.repository.ExhibitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ExhibitionFileService {

    private final ExhibitionFileRepository exhibitionFileRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionFileMapper exhibitionFileMapper;
    private final FileUploadService fileUploadService;
    private final DeepLService deepLService;

    @Transactional(readOnly = true)
    public List<ExhibitionFileDto> findAll(Long exhibitionId, ExhibitionFile.MediaType mediaType) {
        List<ExhibitionFile> files;
        if (mediaType == null) {
            files = exhibitionFileRepository.findByExhibitionIdOrderByDisplayOrderAscIdAsc(exhibitionId);
        } else {
            files = exhibitionFileRepository.findByExhibitionIdAndMediaTypeOrderByDisplayOrderAscIdAsc(exhibitionId, mediaType);
        }
        return files.stream().map(exhibitionFileMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ExhibitionFileDto findById(Long exhibitionId, Long fileId) {
        return exhibitionFileMapper.toDto(getOrThrow(exhibitionId, fileId));
    }

    @CacheEvict(value = {CacheConfig.EXHIBITIONS, CacheConfig.EXHIBITIONS_BY_STATUS}, allEntries = true)
    public ExhibitionFileDto create(Long exhibitionId, ExhibitionFileDto fileDto) {
        if (fileDto.getMediaType() == null || !StringUtils.hasText(fileDto.getFileUrl())) {
            throw new IllegalArgumentException("mediaType and fileUrl are required");
        }

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition not found with id: " + exhibitionId));

        ExhibitionFile file = exhibitionFileMapper.toEntity(fileDto);
        file.setExhibition(exhibition);
        file.applyFileTypeDefault();
        file.setTitleEn(deepLService.translate(file.getTitle()));
        file.setDescriptionEn(deepLService.translate(file.getDescription()));

        if (file.getDisplayOrder() == null) {
            file.setDisplayOrder(exhibitionFileRepository.nextDisplayOrder(exhibitionId));
        }

        return exhibitionFileMapper.toDto(exhibitionFileRepository.save(file));
    }

    @CacheEvict(value = {CacheConfig.EXHIBITIONS, CacheConfig.EXHIBITIONS_BY_STATUS}, allEntries = true)
    public List<ExhibitionFileDto> createAll(Long exhibitionId, List<ExhibitionFileDto> fileDtos) {
        return fileDtos.stream().map(fileDto -> create(exhibitionId, fileDto)).toList();
    }

    @CacheEvict(value = {CacheConfig.EXHIBITIONS, CacheConfig.EXHIBITIONS_BY_STATUS}, allEntries = true)
    public ExhibitionFileDto update(Long exhibitionId, Long fileId, ExhibitionFileDto fileDto) {
        ExhibitionFile file = getOrThrow(exhibitionId, fileId);
        String previousTitle = file.getTitle();
        String previousDescription = file.getDescription();

        exhibitionFileMapper.updateEntityFromDto(fileDto, file);
        file.applyFileTypeDefault();

        file.setTitleEn(deepLService.translateIfChanged(previousTitle, file.getTitle(), file.getTitleEn()));
        file.setDescriptionEn(deepLService.translateIfChanged(previousDescription, file.getDescription(), file.getDescriptionEn()));

        return exhibitionFileMapper.toDto(exhibitionFileRepository.save(file));
    }

    @CacheEvict(value = {CacheConfig.EXHIBITIONS, CacheConfig.EXHIBITIONS_BY_STATUS}, allEntries = true)
    public void delete(Long exhibitionId, Long fileId) {
        ExhibitionFile file = getOrThrow(exhibitionId, fileId);
        fileUploadService.deleteImage(file.getFileUrl());
        fileUploadService.deleteImage(file.getThumbnailUrl());
        exhibitionFileRepository.delete(file);
    }

    @CacheEvict(value = {CacheConfig.EXHIBITIONS, CacheConfig.EXHIBITIONS_BY_STATUS}, allEntries = true)
    public List<ExhibitionFileDto> reorder(Long exhibitionId, List<Long> orderedFileIds) {
        for (int index = 0; index < orderedFileIds.size(); index++) {
            getOrThrow(exhibitionId, orderedFileIds.get(index)).setDisplayOrder(index);
        }
        return findAll(exhibitionId, null);
    }

    private ExhibitionFile getOrThrow(Long exhibitionId, Long fileId) {
        return exhibitionFileRepository.findByIdAndExhibitionId(fileId, exhibitionId)
                .orElseThrow(() -> new EntityNotFoundException("Exhibition file not found with id: " + fileId));
    }
}
