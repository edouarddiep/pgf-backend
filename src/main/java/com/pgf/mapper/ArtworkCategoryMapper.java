package com.pgf.mapper;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.model.ArtworkCategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = ArtworkMapper.class)
public interface ArtworkCategoryMapper {

    @Mapping(target = "artworkCount", expression = "java(category.getArtworks() != null ? (long) category.getArtworks().size() : 0L)")
    ArtworkCategoryDto toDto(ArtworkCategory category);

    @Mapping(target = "artworks", ignore = true)
    ArtworkCategory toEntity(ArtworkCategoryDto categoryDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "artworks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ArtworkCategoryDto categoryDto, @MappingTarget ArtworkCategory category);
}