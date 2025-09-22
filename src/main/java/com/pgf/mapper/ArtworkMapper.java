package com.pgf.mapper;

import com.pgf.dto.ArtworkDto;
import com.pgf.model.Artwork;
import com.pgf.model.ArtworkCategory;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ArtworkMapper {

    @AfterMapping
    default void mapCategories(Artwork artwork, @MappingTarget ArtworkDto dto) {
        if (artwork.getCategories() != null && !artwork.getCategories().isEmpty()) {
            Set<Long> categoryIds = artwork.getCategories().stream()
                    .map(ArtworkCategory::getId)
                    .collect(Collectors.toSet());
            dto.setCategoryIds(categoryIds);

            Set<String> categoryNames = artwork.getCategories().stream()
                    .map(ArtworkCategory::getName)
                    .collect(Collectors.toSet());
            dto.setCategoryNames(categoryNames);

            Set<String> categorySlugs = artwork.getCategories().stream()
                    .map(ArtworkCategory::getSlug)
                    .collect(Collectors.toSet());
            dto.setCategorySlugs(categorySlugs);
        } else {
            dto.setCategoryIds(new HashSet<>());
            dto.setCategoryNames(new HashSet<>());
            dto.setCategorySlugs(new HashSet<>());
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