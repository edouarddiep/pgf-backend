package com.pgf.service;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.dto.ArtworkDto;
import com.pgf.dto.ContactMessageDto;
import com.pgf.dto.ExhibitionDto;
import com.pgf.exception.EntityNotFoundException;
import com.pgf.mapper.ArtworkCategoryMapper;
import com.pgf.mapper.ArtworkMapper;
import com.pgf.mapper.ContactMessageMapper;
import com.pgf.model.Artwork;
import com.pgf.model.ArtworkCategory;
import com.pgf.model.ContactMessage;
import com.pgf.repository.ArtworkCategoryRepository;
import com.pgf.repository.ArtworkRepository;
import com.pgf.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkCategoryRepository categoryRepository;
    private final ContactMessageRepository messageRepository;
    private final ExhibitionService exhibitionService;

    private final ArtworkMapper artworkMapper;
    private final ArtworkCategoryMapper categoryMapper;
    private final ContactMessageMapper messageMapper;

    @Value("${app.admin.password:pgf-admin-2025}")
    private String adminPassword;

    // ===============================================
    // AUTHENTICATION
    // ===============================================

    public boolean validatePassword(String password) {
        boolean isValid = adminPassword.equals(password);
        if (isValid) {
            log.info("Admin authentication successful");
        } else {
            log.warn("Failed admin login attempt");
        }
        return isValid;
    }

    // ===============================================
    // ARTWORKS MANAGEMENT
    // ===============================================

    @Transactional(readOnly = true)
    public List<ArtworkDto> getAllArtworks() {
        List<Artwork> artworks = artworkRepository.findAll();
        return artworks.stream()
                .map(artworkMapper::toDto)
                .toList();
    }

    public ArtworkDto createArtwork(ArtworkDto artworkDto) {
        ArtworkCategory category = categoryRepository.findById(artworkDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Artwork artwork = artworkMapper.toEntity(artworkDto);
        artwork.setCategory(category);

        Artwork saved = artworkRepository.save(artwork);
        log.info("Created artwork: {}", saved.getTitle());

        return artworkMapper.toDto(saved);
    }

    public ArtworkDto updateArtwork(Long id, ArtworkDto artworkDto) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found"));

        ArtworkCategory category = categoryRepository.findById(artworkDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        artworkMapper.updateEntityFromDto(artworkDto, artwork);
        artwork.setCategory(category);

        Artwork updated = artworkRepository.save(artwork);
        log.info("Updated artwork: {}", updated.getTitle());

        return artworkMapper.toDto(updated);
    }

    public void deleteArtwork(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artwork not found"));

        artworkRepository.delete(artwork);
        log.info("Deleted artwork: {}", artwork.getTitle());
    }

    // ===============================================
    // CATEGORIES MANAGEMENT
    // ===============================================

    @Transactional(readOnly = true)
    public List<ArtworkCategoryDto> getAllCategories() {
        List<ArtworkCategory> categories = categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public ArtworkCategoryDto createCategory(ArtworkCategoryDto categoryDto) {
        ArtworkCategory category = categoryMapper.toEntity(categoryDto);
        ArtworkCategory saved = categoryRepository.save(category);

        log.info("Created category: {}", saved.getName());
        return categoryMapper.toDto(saved);
    }

    public ArtworkCategoryDto updateCategory(Long id, ArtworkCategoryDto categoryDto) {
        ArtworkCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        categoryMapper.updateEntityFromDto(categoryDto, category);
        ArtworkCategory updated = categoryRepository.save(category);

        log.info("Updated category: {}", updated.getName());
        return categoryMapper.toDto(updated);
    }

    public void deleteCategory(Long id) {
        ArtworkCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        long artworkCount = artworkRepository.countByCategory(category);
        if (artworkCount > 0) {
            throw new IllegalStateException("Cannot delete category with existing artworks");
        }

        categoryRepository.delete(category);
        log.info("Deleted category: {}", category.getName());
    }

    // ===============================================
    // EXHIBITIONS MANAGEMENT
    // ===============================================

    @Transactional(readOnly = true)
    public List<ExhibitionDto> getAllExhibitions() {
        return exhibitionService.findAll();
    }

    public ExhibitionDto createExhibition(ExhibitionDto exhibitionDto) {
        return exhibitionService.create(exhibitionDto);
    }

    public ExhibitionDto updateExhibition(Long id, ExhibitionDto exhibitionDto) {
        return exhibitionService.update(id, exhibitionDto);
    }

    public void deleteExhibition(Long id) {
        exhibitionService.delete(id);
    }

    // ===============================================
    // CONTACT MESSAGES MANAGEMENT
    // ===============================================

    @Transactional(readOnly = true)
    public List<ContactMessageDto> getAllMessages() {
        List<ContactMessage> messages = messageRepository.findAllByOrderByCreatedAtDesc();
        return messages.stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDto> getUnreadMessages() {
        List<ContactMessage> messages = messageRepository.findByIsReadFalseOrderByCreatedAtDesc();
        return messages.stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadMessagesCount() {
        return messageRepository.countByIsReadFalse();
    }

    public ContactMessageDto markMessageAsRead(Long id) {
        ContactMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        message.setIsRead(true);
        ContactMessage updated = messageRepository.save(message);

        log.info("Marked message as read: {}", id);
        return messageMapper.toDto(updated);
    }

    public void deleteMessage(Long id) {
        ContactMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        messageRepository.delete(message);
        log.info("Deleted message: {}", id);
    }
}