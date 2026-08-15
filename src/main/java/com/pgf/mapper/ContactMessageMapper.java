package com.pgf.mapper;

import com.pgf.dto.ContactMessageDto;
import com.pgf.model.ContactMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactMessageMapper {

    ContactMessageDto toDto(ContactMessage contactMessage);

    ContactMessage toEntity(ContactMessageDto contactMessageDto);
}
