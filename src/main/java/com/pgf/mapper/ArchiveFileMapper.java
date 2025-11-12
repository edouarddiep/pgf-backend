package com.pgf.mapper;

import com.pgf.dto.ArchiveFileDto;
import com.pgf.model.ArchiveFile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ArchiveFileMapper {

    ArchiveFileDto toDto(ArchiveFile archiveFile);

    @Mapping(target = "archive", ignore = true)
    ArchiveFile toEntity(ArchiveFileDto archiveFileDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "archive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ArchiveFileDto dto, @MappingTarget ArchiveFile entity);
}