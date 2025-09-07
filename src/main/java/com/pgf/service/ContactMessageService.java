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

    private final ContactMessageRepository messageRepository;
    private final ContactMessageMapper messageMapper;

    @Transactional(readOnly = true)
    public List<ContactMessageDto> findAll() {
        return messageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactMessageDto findById(Long id) {
        ContactMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));
        return messageMapper.toDto(message);
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDto> findByStatus(ContactMessage.MessageStatus status) {
        return messageRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDto> findUnread() {
        return messageRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return messageRepository.countUnreadMessages();
    }

    public ContactMessageDto create(ContactMessageDto messageDto) {
        ContactMessage message = messageMapper.toEntity(messageDto);
        ContactMessage savedMessage = messageRepository.save(message);
        return messageMapper.toDto(savedMessage);
    }

    public ContactMessageDto markAsRead(Long id) {
        ContactMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));

        message.setIsRead(true);
        if (message.getStatus() == ContactMessage.MessageStatus.NEW) {
            message.setStatus(ContactMessage.MessageStatus.READ);
        }

        ContactMessage updatedMessage = messageRepository.save(message);
        return messageMapper.toDto(updatedMessage);
    }

    public ContactMessageDto updateStatus(Long id, ContactMessage.MessageStatus status) {
        ContactMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));

        message.setStatus(status);
        ContactMessage updatedMessage = messageRepository.save(message);
        return messageMapper.toDto(updatedMessage);
    }

    public void delete(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new EntityNotFoundException("Message not found with id: " + id);
        }
        messageRepository.deleteById(id);
    }
}