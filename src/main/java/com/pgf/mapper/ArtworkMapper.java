package com.pgf.mapper;

import com.pgf.dto.ArtworkDto;
import com.pgf.model.Artwork;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ArtworkMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.slug", target = "categorySlug")
    ArtworkDto toDto(Artwork artwork);

    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "category.name", ignore = true)
    @Mapping(target = "category.slug", ignore = true)
    @Mapping(target = "category.description", ignore = true)
    @Mapping(target = "category.displayOrder", ignore = true)
    @Mapping(target = "category.artworks", ignore = true)
    @Mapping(target = "category.createdAt", ignore = true)
    @Mapping(target = "category.updatedAt", ignore = true)
    Artwork toEntity(ArtworkDto artworkDto);

    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "category.name", ignore = true)
    @Mapping(target = "category.slug", ignore = true)
    @Mapping(target = "category.description", ignore = true)
    @Mapping(target = "category.displayOrder", ignore = true)
    @Mapping(target = "category.artworks", ignore = true)
    @Mapping(target = "category.createdAt", ignore = true)
    @Mapping(target = "category.updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ArtworkDto artworkDto, @MappingTarget Artwork artwork);
}