package com.pgf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgf.dto.ArchiveDto;
import com.pgf.dto.ArtworkCategoryDto;
import com.pgf.dto.ArtworkDto;
import com.pgf.dto.ContactMessageDto;
import com.pgf.dto.ExhibitionDto;
import com.pgf.model.Exhibition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

    // ===============================================
    // CATEGORIES
    // ===============================================

    @Test
    void shouldGetAllArtworkCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetCategoryBySlug() throws Exception {
        mockMvc.perform(get("/api/categories/slug/fils-de-fer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fils de fer"))
                .andExpect(jsonPath("$.slug").value("fils-de-fer"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentCategory() throws Exception {
        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateNewCategory() throws Exception {
        ArtworkCategoryDto newCategory = new ArtworkCategoryDto();
        newCategory.setName("Test Category");
        newCategory.setDescription("Category for testing");
        newCategory.setDescriptionShort("Test category short");
        newCategory.setSlug("test-category");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.slug").value("test-category"));
    }

    // ===============================================
    // ARTWORKS
    // ===============================================

    @Test
    void shouldGetAllArtworks() throws Exception {
        mockMvc.perform(get("/api/artworks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnNotFoundForNonExistentArtwork() throws Exception {
        mockMvc.perform(get("/api/artworks/999"))
                .andExpect(status().isNotFound());
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
    void shouldCreateNewArtwork() throws Exception {
        ArtworkDto newArtwork = new ArtworkDto();
        newArtwork.setTitle("Test Artwork");
        newArtwork.setDescription("A beautiful test artwork");
        newArtwork.setDescriptionShort("Beautiful artwork");
        newArtwork.setImageUrls(List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));
        newArtwork.setMainImageUrl("https://example.com/image1.jpg");
        newArtwork.setCategoryIds(Set.of(1L));

        mockMvc.perform(post("/api/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newArtwork)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Artwork"))
                .andExpect(jsonPath("$.imageUrls").isArray())
                .andExpect(jsonPath("$.imageUrls.length()").value(2))
                .andExpect(jsonPath("$.mainImageUrl").value("https://example.com/image1.jpg"));
    }

    @Test
    void shouldCreateArtworkWithMultipleCategories() throws Exception {
        ArtworkDto newArtwork = new ArtworkDto();
        newArtwork.setTitle("Multi-Category Artwork");
        newArtwork.setDescription("Artwork belonging to multiple categories");
        newArtwork.setImageUrls(List.of("https://example.com/image1.jpg"));
        newArtwork.setMainImageUrl("https://example.com/image1.jpg");
        newArtwork.setCategoryIds(Set.of(1L, 2L));

        mockMvc.perform(post("/api/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newArtwork)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Multi-Category Artwork"))
                .andExpect(jsonPath("$.categoryIds").isArray())
                .andExpect(jsonPath("$.categoryIds.length()").value(2));
    }

    @Test
    void shouldValidateRequiredFieldsForNewArtwork() throws Exception {
        mockMvc.perform(post("/api/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ArtworkDto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateRequiredCategoriesForArtwork() throws Exception {
        ArtworkDto artworkWithoutCategories = new ArtworkDto();
        artworkWithoutCategories.setTitle("Test Artwork");
        artworkWithoutCategories.setDescription("Test description");
        artworkWithoutCategories.setImageUrls(List.of("https://example.com/image.jpg"));
        artworkWithoutCategories.setCategoryIds(Set.of());

        mockMvc.perform(post("/api/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(artworkWithoutCategories)))
                .andExpect(status().isBadRequest());
    }

    // ===============================================
    // EXHIBITIONS
    // ===============================================

    @Test
    void shouldGetAllExhibitions() throws Exception {
        mockMvc.perform(get("/api/exhibitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
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
    void shouldCreateNewExhibition() throws Exception {
        ExhibitionDto newExhibition = new ExhibitionDto();
        newExhibition.setTitle("Test Exhibition");
        newExhibition.setDescription("A wonderful test exhibition");
        newExhibition.setLocation("Test Gallery");
        newExhibition.setAddress("123 Art Street, Paris");
        newExhibition.setStartDate(LocalDate.now().plusDays(30));
        newExhibition.setEndDate(LocalDate.now().plusDays(60));
        newExhibition.setStatus(Exhibition.ExhibitionStatus.UPCOMING);

        mockMvc.perform(post("/api/exhibitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newExhibition)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Exhibition"))
                .andExpect(jsonPath("$.location").value("Test Gallery"))
                .andExpect(jsonPath("$.address").value("123 Art Street, Paris"));
    }

    // ===============================================
    // ARCHIVES
    // ===============================================

    @Test
    void shouldGetAllArchives() throws Exception {
        mockMvc.perform(get("/api/archives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnNotFoundForNonExistentArchive() throws Exception {
        mockMvc.perform(get("/api/archives/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateArchive() throws Exception {
        ArchiveDto dto = new ArchiveDto();
        dto.setTitle("Test Archive");
        dto.setYear(2024);
        dto.setDescription("Archive de test");
        dto.setThumbnailUrl("https://example.com/thumb.jpg");
        dto.setFiles(List.of());

        mockMvc.perform(post("/api/archives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Archive"))
                .andExpect(jsonPath("$.year").value(2024));
    }

    @Test
    void shouldDeleteArchiveViaAdmin() throws Exception {
        ArchiveDto dto = new ArchiveDto();
        dto.setTitle("Archive à supprimer");
        dto.setYear(2023);
        dto.setThumbnailUrl("https://example.com/thumb.jpg");
        dto.setFiles(List.of());

        String response = mockMvc.perform(post("/api/archives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ArchiveDto created = objectMapper.readValue(response, ArchiveDto.class);

        mockMvc.perform(delete("/api/admin/archives/" + created.getId()))
                .andExpect(status().isNoContent());
    }

    // ===============================================
    // CONTACT
    // ===============================================

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
    void shouldValidateRequiredFieldsForContactMessage() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContactMessageDto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteContactMessage() throws Exception {
        ContactMessageDto message = new ContactMessageDto();
        message.setName("Jane Doe");
        message.setEmail("jane@example.com");
        message.setSubject("Test");
        message.setMessage("Message à supprimer.");

        String response = mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ContactMessageDto created = objectMapper.readValue(response, ContactMessageDto.class);

        mockMvc.perform(delete("/api/admin/messages/" + created.getId()))
                .andExpect(status().isNoContent());
    }

    // ===============================================
    // ADMIN
    // ===============================================

    @Test
    void shouldLoginAsAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"pgf-admin-2025\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidAdminPassword() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetArtworksForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/artworks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldCreateArtworkViaAdmin() throws Exception {
        ArtworkDto newArtwork = new ArtworkDto();
        newArtwork.setTitle("Admin Test Artwork");
        newArtwork.setDescription("Created via admin endpoint");
        newArtwork.setDescriptionShort("Admin artwork");
        newArtwork.setImageUrls(List.of("https://example.com/admin-image.jpg"));
        newArtwork.setMainImageUrl("https://example.com/admin-image.jpg");
        newArtwork.setCategoryIds(Set.of(1L, 2L));

        mockMvc.perform(post("/api/admin/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newArtwork)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Admin Test Artwork"))
                .andExpect(jsonPath("$.categoryIds").isArray())
                .andExpect(jsonPath("$.categoryIds.length()").value(2));
    }

    @Test
    void shouldUpdateArtworkCategories() throws Exception {
        ArtworkDto artwork = new ArtworkDto();
        artwork.setTitle("Test Artwork for Category Update");
        artwork.setImageUrls(List.of("https://example.com/image.jpg"));
        artwork.setMainImageUrl("https://example.com/image.jpg");
        artwork.setCategoryIds(Set.of(1L));

        String response = mockMvc.perform(post("/api/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(artwork)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ArtworkDto created = objectMapper.readValue(response, ArtworkDto.class);

        mockMvc.perform(put("/api/admin/artworks/" + created.getId() + "/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of(1L, 2L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryIds").isArray())
                .andExpect(jsonPath("$.categoryIds.length()").value(3));
    }

    @Test
    void shouldGetArchivesForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/archives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldCreateArchiveViaAdmin() throws Exception {
        ArchiveDto dto = new ArchiveDto();
        dto.setTitle("Admin Archive");
        dto.setYear(2025);
        dto.setThumbnailUrl("https://example.com/thumb.jpg");
        dto.setFiles(List.of());

        mockMvc.perform(post("/api/admin/archives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Admin Archive"))
                .andExpect(jsonPath("$.year").value(2025));
    }
}