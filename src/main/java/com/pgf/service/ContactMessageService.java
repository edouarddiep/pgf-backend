package com.pgf.service;

import com.pgf.dto.ContactMessageDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ContactMessageMapper;
import com.pgf.model.ContactMessage;
import com.pgf.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final ContactMessageMapper contactMessageMapper;
    private final MailNotificationService mailNotificationService;

    @Transactional(readOnly = true)
    public List<ContactMessageDto> findAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(contactMessageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactMessageDto findById(Long id) {
        return contactMessageMapper.toDto(getOrThrow(id));
    }

    public ContactMessageDto create(ContactMessageDto messageDto) {
        ContactMessage message = contactMessageMapper.toEntity(messageDto);
        message.setIsRead(false);
        message.setStatus(ContactMessage.MessageStatus.NEW);

        ContactMessageDto created = contactMessageMapper.toDto(contactMessageRepository.save(message));
        mailNotificationService.sendContactNotification(created);
        return created;
    }

    public ContactMessageDto markAsRead(Long id) {
        ContactMessage message = getOrThrow(id);
        message.setIsRead(true);
        if (message.getStatus() == ContactMessage.MessageStatus.NEW) {
            message.setStatus(ContactMessage.MessageStatus.READ);
        }
        return contactMessageMapper.toDto(contactMessageRepository.save(message));
    }

    public void delete(Long id) {
        contactMessageRepository.delete(getOrThrow(id));
    }

    private ContactMessage getOrThrow(Long id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));
    }
}
