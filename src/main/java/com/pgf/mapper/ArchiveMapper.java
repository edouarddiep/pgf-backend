package com.pgf.mapper;

import com.pgf.dto.ArchiveDto;
import com.pgf.model.Archive;
import com.pgf.util.SupabaseImageUrls;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = ArchiveFileMapper.class, imports = SupabaseImageUrls.class)
public interface ArchiveMapper {

    @Mapping(target = "thumbnailSrcset", expression = "java(SupabaseImageUrls.srcSet(archive.getThumbnailUrl()))")
    ArchiveDto toDto(Archive archive);

    Archive toEntity(ArchiveDto archiveDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "titleEn", ignore = true)
    @Mapping(target = "descriptionEn", ignore = true)
    void updateEntityFromDto(ArchiveDto archiveDto, @MappingTarget Archive archive);
}