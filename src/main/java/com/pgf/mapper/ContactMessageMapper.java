package com.pgf.mapper;

import com.pgf.dto.ContactMessageDto;
import com.pgf.model.ContactMessage;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ContactMessageMapper {

    ContactMessageDto toDto(ContactMessage contactMessage);

    ContactMessage toEntity(ContactMessageDto contactMessageDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ContactMessageDto contactMessageDto, @MappingTarget ContactMessage contactMessage);
}