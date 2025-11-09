package com.pgf.service;

import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.dto.ArtworkDto;
import com.pgf.dto.ContactMessageDto;
import com.pgf.dto.ExhibitionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminService {

    private final ArtworkService artworkService;
    private final ArtworkCategoryService categoryService;
    private final ExhibitionService exhibitionService;
    private final ContactMessageService messageService;

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
    // ARTWORKS MANAGEMENT (Délégation)
    // ===============================================

    @Transactional(readOnly = true)
    public List<ArtworkDto> getAllArtworks() {
        return artworkService.findAll();
    }

    public ArtworkDto createArtwork(ArtworkDto artworkDto) {
        return artworkService.create(artworkDto);
    }

    public ArtworkDto updateArtwork(Long id, ArtworkDto artworkDto) {
        return artworkService.update(id, artworkDto);
    }

    public ArtworkDto updateArtworkCategories(Long artworkId, Set<Long> categoryIds) {
        return artworkService.updateArtworkCategories(artworkId, categoryIds);
    }

    public void deleteArtwork(Long id) {
        artworkService.delete(id);
    }

    // ===============================================
    // CATEGORIES MANAGEMENT (Délégation)
    // ===============================================

    @Transactional(readOnly = true)
    public List<ArtworkCategoryDto> getAllCategories() {
        return categoryService.findAll();
    }

    public ArtworkCategoryDto createCategory(ArtworkCategoryDto categoryDto) {
        return categoryService.create(categoryDto);
    }

    public ArtworkCategoryDto updateCategory(Long id, ArtworkCategoryDto categoryDto) {
        return categoryService.update(id, categoryDto);
    }

    public void deleteCategory(Long id) {
        categoryService.delete(id);
    }

    // ===============================================
    // EXHIBITIONS MANAGEMENT (Délégation)
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
    // CONTACT MESSAGES MANAGEMENT (Délégation)
    // ===============================================

    @Transactional(readOnly = true)
    public List<ContactMessageDto> getAllMessages() {
        return messageService.findAll();
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDto> getUnreadMessages() {
        return messageService.findUnreadMessages();
    }

    @Transactional(readOnly = true)
    public long getUnreadMessagesCount() {
        return messageService.countUnreadMessages();
    }

    public ContactMessageDto markMessageAsRead(Long id) {
        return messageService.markAsRead(id);
    }

    public void deleteMessage(Long id) {
        messageService.delete(id);
    }
}