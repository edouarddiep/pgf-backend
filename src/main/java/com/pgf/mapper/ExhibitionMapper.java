package com.pgf.mapper;

import com.pgf.dto.ExhibitionDto;
import com.pgf.model.Exhibition;
import com.pgf.util.SupabaseImageUrls;
import org.mapstruct.*;

@Mapper(componentModel = "spring", imports = SupabaseImageUrls.class)
public interface ExhibitionMapper {

    @Mapping(target = "imageSrcset", expression = "java(SupabaseImageUrls.srcSet(exhibition.getImageUrl()))")
    @Mapping(target = "imageSrcsets", expression = "java(SupabaseImageUrls.srcSets(exhibition.getImageUrls()))")
    ExhibitionDto toDto(Exhibition exhibition);

    Exhibition toEntity(ExhibitionDto exhibitionDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "titleEn", ignore = true)
    @Mapping(target = "descriptionEn", ignore = true)
    void updateEntityFromDto(ExhibitionDto exhibitionDto, @MappingTarget Exhibition exhibition);
}