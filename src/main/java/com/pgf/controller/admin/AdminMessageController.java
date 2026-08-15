package com.pgf.controller.admin;

import com.pgf.dto.ContactMessageDto;
import com.pgf.model.ContactMessage;
import com.pgf.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/messages")
@Tag(name = "Admin - Messages", description = "Contact message administration")
@RequiredArgsConstructor
public class AdminMessageController {

    private final ContactMessageService messageService;

    @GetMapping
    @Operation(summary = "Get contact messages, optionally filtered by status")
    public List<ContactMessageDto> findAll(@RequestParam(required = false) ContactMessage.MessageStatus status) {
        return status == null ? messageService.findAll() : messageService.findByStatus(status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID")
    public ContactMessageDto findById(@PathVariable Long id) {
        return messageService.findById(id);
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread messages")
    public List<ContactMessageDto> findUnread() {
        return messageService.findUnread();
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Count unread messages")
    public long countUnread() {
        return messageService.countUnread();
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark message as read")
    public ContactMessageDto markAsRead(@PathVariable Long id) {
        return messageService.markAsRead(id);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update message status")
    public ContactMessageDto updateStatus(@PathVariable Long id, @RequestBody ContactMessage.MessageStatus status) {
        return messageService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete message")
    public void delete(@PathVariable Long id) {
        messageService.delete(id);
    }
}
