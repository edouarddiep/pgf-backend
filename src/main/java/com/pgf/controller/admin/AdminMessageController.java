package com.pgf.controller.admin;

import com.pgf.dto.ContactMessageDto;
import com.pgf.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @Operation(summary = "Get all contact messages")
    public List<ContactMessageDto> findAll() {
        return messageService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID")
    public ContactMessageDto findById(@PathVariable Long id) {
        return messageService.findById(id);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark message as read")
    public ContactMessageDto markAsRead(@PathVariable Long id) {
        return messageService.markAsRead(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete message")
    public void delete(@PathVariable Long id) {
        messageService.delete(id);
    }
}
