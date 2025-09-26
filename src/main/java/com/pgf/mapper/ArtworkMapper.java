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

    @Mapping(target = "categoryIds", expression = "java(mapCategoryIds(artwork))")
    @Mapping(target = "categoryNames", expression = "java(mapCategoryNames(artwork))")
    @Mapping(target = "categorySlugs", expression = "java(mapCategorySlugs(artwork))")
    ArtworkDto toDto(Artwork artwork);

    default Set<Long> mapCategoryIds(Artwork artwork) {
        System.out.println("DEBUG - mapCategoryIds called for: " + artwork.getTitle());
        if (artwork.getCategories() != null && !artwork.getCategories().isEmpty()) {
            Set<Long> ids = artwork.getCategories().stream()
                    .map(ArtworkCategory::getId)
                    .collect(Collectors.toSet());
            System.out.println("DEBUG - Mapped category IDs: " + ids);
            return ids;
        }
        System.out.println("DEBUG - No categories, returning empty set");
        return new HashSet<>();
    }

    default Set<String> mapCategoryNames(Artwork artwork) {
        if (artwork.getCategories() != null && !artwork.getCategories().isEmpty()) {
            return artwork.getCategories().stream()
                    .map(ArtworkCategory::getName)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    default Set<String> mapCategorySlugs(Artwork artwork) {
        if (artwork.getCategories() != null && !artwork.getCategories().isEmpty()) {
            return artwork.getCategories().stream()
                    .map(ArtworkCategory::getSlug)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    @Mapping(target = "categories", ignore = true)
    Artwork toEntity(ArtworkDto artworkDto);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ArtworkDto artworkDto, @MappingTarget Artwork artwork);
}