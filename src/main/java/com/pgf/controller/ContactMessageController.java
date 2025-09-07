package com.pgf.controller;

import com.pgf.dto.ContactMessageDto;
import com.pgf.model.ContactMessage;
import com.pgf.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
@Tag(name = "Contact", description = "Contact message management endpoints")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService messageService;

    @GetMapping("/messages")
    @Operation(summary = "Get all contact messages")
    public ResponseEntity<List<ContactMessageDto>> getAllMessages() {
        List<ContactMessageDto> messages = messageService.findAll();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/{id}")
    @Operation(summary = "Get message by ID")
    public ResponseEntity<ContactMessageDto> getMessageById(@PathVariable Long id) {
        ContactMessageDto message = messageService.findById(id);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/messages/unread")
    @Operation(summary = "Get unread messages")
    public ResponseEntity<List<ContactMessageDto>> getUnreadMessages() {
        List<ContactMessageDto> messages = messageService.findUnread();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/count-unread")
    @Operation(summary = "Get unread messages count")
    public ResponseEntity<Long> getUnreadCount() {
        long count = messageService.countUnread();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/messages/status/{status}")
    @Operation(summary = "Get messages by status")
    public ResponseEntity<List<ContactMessageDto>> getMessagesByStatus(@PathVariable ContactMessage.MessageStatus status) {
        List<ContactMessageDto> messages = messageService.findByStatus(status);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    @Operation(summary = "Send contact message")
    public ResponseEntity<ContactMessageDto> sendMessage(@Valid @RequestBody ContactMessageDto messageDto) {
        ContactMessageDto createdMessage = messageService.create(messageDto);
        return new ResponseEntity<>(createdMessage, HttpStatus.CREATED);
    }

    @PutMapping("/messages/{id}/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<ContactMessageDto> markAsRead(@PathVariable Long id) {
        ContactMessageDto updatedMessage = messageService.markAsRead(id);
        return ResponseEntity.ok(updatedMessage);
    }

    @PutMapping("/messages/{id}/status")
    @Operation(summary = "Update message status")
    public ResponseEntity<ContactMessageDto> updateStatus(@PathVariable Long id, @RequestBody ContactMessage.MessageStatus status) {
        ContactMessageDto updatedMessage = messageService.updateStatus(id, status);
        return ResponseEntity.ok(updatedMessage);
    }

    @DeleteMapping("/messages/{id}")
    @Operation(summary = "Delete message")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}