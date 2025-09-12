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

    @Transactional(readOnly = true)
    public List<ContactMessageDto> findAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(contactMessageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactMessageDto findById(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));
        return contactMessageMapper.toDto(message);
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDto> findUnread() {
        return contactMessageRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(contactMessageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDto> findUnreadMessages() {
        return contactMessageRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(contactMessageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return contactMessageRepository.countByIsReadFalse();
    }

    @Transactional(readOnly = true)
    public Long countUnreadMessages() {
        return contactMessageRepository.countByIsReadFalse();
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDto> findByStatus(ContactMessage.MessageStatus status) {
        return contactMessageRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(contactMessageMapper::toDto)
                .toList();
    }

    public ContactMessageDto create(ContactMessageDto messageDto) {
        ContactMessage message = contactMessageMapper.toEntity(messageDto);
        message.setIsRead(false);
        message.setStatus(ContactMessage.MessageStatus.NEW);
        ContactMessage savedMessage = contactMessageRepository.save(message);
        return contactMessageMapper.toDto(savedMessage);
    }

    public ContactMessageDto markAsRead(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));
        message.setIsRead(true);
        if (message.getStatus() == ContactMessage.MessageStatus.NEW) {
            message.setStatus(ContactMessage.MessageStatus.READ);
        }
        ContactMessage updatedMessage = contactMessageRepository.save(message);
        return contactMessageMapper.toDto(updatedMessage);
    }

    public ContactMessageDto updateStatus(Long id, ContactMessage.MessageStatus status) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));
        message.setStatus(status);
        ContactMessage updatedMessage = contactMessageRepository.save(message);
        return contactMessageMapper.toDto(updatedMessage);
    }

    public void delete(Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw new EntityNotFoundException("Message not found with id: " + id);
        }
        contactMessageRepository.deleteById(id);
    }
}