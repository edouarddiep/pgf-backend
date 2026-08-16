package com.pgf.mapper;

import com.pgf.dto.ArchiveFileDto;
import com.pgf.model.ArchiveFile;
import com.pgf.util.SupabaseImageUrls;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArchiveFileMapper {

    @Mapping(target = "fileSrcset", expression = "java(imageSrcSet(archiveFile))")
    ArchiveFileDto toDto(ArchiveFile archiveFile);

    default String imageSrcSet(ArchiveFile archiveFile) {
        if (archiveFile.getFileType() != ArchiveFile.FileType.IMAGE) {
            return null;
        }
        return SupabaseImageUrls.srcSet(archiveFile.getFileUrl());
    }

    @Mapping(target = "archive", ignore = true)
    ArchiveFile toEntity(ArchiveFileDto archiveFileDto);
}
