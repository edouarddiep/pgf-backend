package com.pgf.mapper;

import com.pgf.dto.ArtworkDto;
import com.pgf.model.Artwork;
import com.pgf.model.ArtworkCategory;
import org.mapstruct.*;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ArtworkMapper {

    @AfterMapping
    default void mapCategories(Artwork artwork, @MappingTarget ArtworkDto dto) {
        if (artwork.getCategories() != null) {
            dto.setCategoryIds(artwork.getCategories().stream()
                    .map(ArtworkCategory::getId)
                    .collect(Collectors.toSet()));

            dto.setCategoryNames(artwork.getCategories().stream()
                    .map(ArtworkCategory::getName)
                    .collect(Collectors.toSet()));

            dto.setCategorySlugs(artwork.getCategories().stream()
                    .map(ArtworkCategory::getSlug)
                    .collect(Collectors.toSet()));
        }
    }

    ArtworkDto toDto(Artwork artwork);

    @Mapping(target = "categories", ignore = true)
    Artwork toEntity(ArtworkDto artworkDto);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ArtworkDto artworkDto, @MappingTarget Artwork artwork);
}