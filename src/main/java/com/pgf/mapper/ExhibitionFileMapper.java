package com.pgf.mapper;

import com.pgf.dto.ExhibitionFileDto;
import com.pgf.model.ExhibitionFile;
import com.pgf.util.SupabaseImageUrls;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", imports = SupabaseImageUrls.class)
public interface ExhibitionFileMapper {

    @Mapping(target = "fileSrcset", expression = "java(fileSrcSet(exhibitionFile))")
    @Mapping(target = "thumbnailSrcset", expression = "java(SupabaseImageUrls.srcSet(exhibitionFile.getThumbnailUrl()))")
    ExhibitionFileDto toDto(ExhibitionFile exhibitionFile);

    @Mapping(target = "exhibition", ignore = true)
    ExhibitionFile toEntity(ExhibitionFileDto exhibitionFileDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "exhibition", ignore = true)
    @Mapping(target = "titleEn", ignore = true)
    @Mapping(target = "descriptionEn", ignore = true)
    void updateEntityFromDto(ExhibitionFileDto exhibitionFileDto, @MappingTarget ExhibitionFile exhibitionFile);

    default String fileSrcSet(ExhibitionFile exhibitionFile) {
        if (exhibitionFile.getFileType() != ExhibitionFile.FileType.IMAGE) {
            return null;
        }
        return SupabaseImageUrls.srcSet(exhibitionFile.getFileUrl());
    }
}
