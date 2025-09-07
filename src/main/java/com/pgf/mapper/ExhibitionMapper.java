package com.pgf.mapper;

import com.pgf.dto.ExhibitionDto;
import com.pgf.model.Exhibition;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExhibitionMapper {

    ExhibitionDto toDto(Exhibition exhibition);

    Exhibition toEntity(ExhibitionDto exhibitionDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ExhibitionDto exhibitionDto, @MappingTarget Exhibition exhibition);
}