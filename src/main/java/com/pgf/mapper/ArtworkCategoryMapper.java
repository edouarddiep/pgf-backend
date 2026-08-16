package com.pgf.mapper;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.model.ArtworkCategory;
import com.pgf.util.SupabaseImageUrls;
import org.mapstruct.*;

@Mapper(componentModel = "spring", imports = SupabaseImageUrls.class)
public interface ArtworkCategoryMapper {

    @Mapping(target = "artworkCount", expression = "java(category.getArtworks() != null ? category.getArtworks().size() : 0)")
    @Mapping(target = "thumbnailSrcset", expression = "java(SupabaseImageUrls.srcSet(category.getThumbnailUrl()))")
    ArtworkCategoryDto toDto(ArtworkCategory category);

    @Mapping(target = "artworks", ignore = true)
    ArtworkCategory toEntity(ArtworkCategoryDto categoryDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "artworks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "displayOrder", ignore = true)
    @Mapping(target = "nameEn", ignore = true)
    @Mapping(target = "descriptionEn", ignore = true)
    void updateEntityFromDto(ArtworkCategoryDto categoryDto, @MappingTarget ArtworkCategory category);
}