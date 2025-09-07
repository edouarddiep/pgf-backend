package com.pgf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.dto.ArtworkDto;
import com.pgf.dto.ContactMessageDto;
import com.pgf.dto.ExhibitionDto;
import com.pgf.model.ContactMessage;
import com.pgf.model.Exhibition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PgfIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAllArtworkCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5)) // 5 catégories par défaut
                .andExpect(jsonPath("$[0].name").value("Fils de fer"));
    }

    @Test
    void shouldGetCategoryBySlug() throws Exception {
        mockMvc.perform(get("/api/categories/slug/fils-de-fer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fils de fer"))
                .andExpect(jsonPath("$.slug").value("fils-de-fer"));
    }

    @Test
    void shouldCreateNewCategory() throws Exception {
        ArtworkCategoryDto newCategory = new ArtworkCategoryDto();
        newCategory.setName("Test Category");
        newCategory.setDescription("Category for testing");
        newCategory.setSlug("test-category");
        newCategory.setDisplayOrder(10);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.slug").value("test-category"));
    }

    @Test
    void shouldGetAllArtworks() throws Exception {
        mockMvc.perform(get("/api/artworks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldCreateNewArtwork() throws Exception {
        ArtworkDto newArtwork = new ArtworkDto();
        newArtwork.setTitle("Test Artwork");
        newArtwork.setDescription("A beautiful test artwork");
        newArtwork.setDimensions("30x40cm");
        newArtwork.setMaterials("Oil on canvas");
        newArtwork.setCreationDate(LocalDate.now());
        newArtwork.setPrice(new BigDecimal("500.00"));
        newArtwork.setIsAvailable(true);
        newArtwork.setCategoryId(1L); // Fils de fer

        mockMvc.perform(post("/api/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newArtwork)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Artwork"))
                .andExpect(jsonPath("$.price").value(500.00));
    }

    @Test
    void shouldGetArtworksByCategory() throws Exception {
        mockMvc.perform(get("/api/artworks/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetArtworksByCategorySlug() throws Exception {
        mockMvc.perform(get("/api/artworks/category/slug/fils-de-fer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetAvailableArtworks() throws Exception {
        mockMvc.perform(get("/api/artworks/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetAllExhibitions() throws Exception {
        mockMvc.perform(get("/api/exhibitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldCreateNewExhibition() throws Exception {
        ExhibitionDto newExhibition = new ExhibitionDto();
        newExhibition.setTitle("Test Exhibition");
        newExhibition.setDescription("A wonderful test exhibition");
        newExhibition.setLocation("Test Gallery");
        newExhibition.setStartDate(LocalDate.now().plusDays(30));
        newExhibition.setEndDate(LocalDate.now().plusDays(60));
        newExhibition.setIsFeatured(true);
        newExhibition.setStatus(Exhibition.ExhibitionStatus.UPCOMING);

        mockMvc.perform(post("/api/exhibitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newExhibition)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Exhibition"))
                .andExpect(jsonPath("$.isFeatured").value(true));
    }

    @Test
    void shouldGetUpcomingExhibitions() throws Exception {
        mockMvc.perform(get("/api/exhibitions/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetPastExhibitions() throws Exception {
        mockMvc.perform(get("/api/exhibitions/past"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldSendContactMessage() throws Exception {
        ContactMessageDto message = new ContactMessageDto();
        message.setName("John Doe");
        message.setEmail("john.doe@example.com");
        message.setPhone("0123456789");
        message.setSubject("Interested in artwork");
        message.setMessage("I am interested in purchasing one of your artworks.");

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.isRead").value(false))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void shouldGetAllContactMessages() throws Exception {
        mockMvc.perform(get("/api/contact/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetUnreadMessages() throws Exception {
        mockMvc.perform(get("/api/contact/messages/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetUnreadCount() throws Exception {
        mockMvc.perform(get("/api/contact/messages/count-unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void shouldUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(file)
                        .param("category", "artworks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image uploadée avec succès"))
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.fileName").value("test-image.jpg"));
    }

    @Test
    void shouldUploadImageWithThumbnail() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "fake image content for thumbnail test".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/image-with-thumbnail")
                        .file(file)
                        .param("category", "exhibitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image et thumbnail uploadés avec succès"))
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.thumbnailUrl").exists())
                .andExpect(jsonPath("$.fileName").exists())
                .andExpect(jsonPath("$.fileSize").isNumber());
    }

    @Test
    void shouldReturnNotFoundForNonExistentCategory() throws Exception {
        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnNotFoundForNonExistentArtwork() throws Exception {
        mockMvc.perform(get("/api/artworks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateRequiredFieldsForNewArtwork() throws Exception {
        ArtworkDto invalidArtwork = new ArtworkDto();
        // Pas de titre ni de categoryId

        mockMvc.perform(post("/api/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidArtwork)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateRequiredFieldsForContactMessage() throws Exception {
        ContactMessageDto invalidMessage = new ContactMessageDto();
        // Pas de nom, email, ou message

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMessage)))
                .andExpect(status().isBadRequest());
    }
}