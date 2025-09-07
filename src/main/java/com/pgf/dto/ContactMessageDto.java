package com.pgf.dto;

import com.pgf.model.ContactMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContactMessageDto {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private String phone;

    private String subject;

    @NotBlank
    private String message;

    private Boolean isRead;

    private ContactMessage.MessageStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}