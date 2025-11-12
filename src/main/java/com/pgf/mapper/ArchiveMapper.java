package com.pgf.mapper;

import com.pgf.dto.ArchiveDto;
import com.pgf.model.Archive;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = ArchiveFileMapper.class)
public interface ArchiveMapper {

    ArchiveDto toDto(Archive archive);

    Archive toEntity(ArchiveDto archiveDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ArchiveDto archiveDto, @MappingTarget Archive archive);
}