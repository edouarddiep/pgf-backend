package com.pgf.mapper;

import com.pgf.dto.ArchiveFileDto;
import com.pgf.model.ArchiveFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArchiveFileMapper {

    ArchiveFileDto toDto(ArchiveFile archiveFile);

    @Mapping(target = "archive", ignore = true)
    ArchiveFile toEntity(ArchiveFileDto archiveFileDto);
}
