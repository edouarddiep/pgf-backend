package com.pgf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pgf.model.ExhibitionFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExhibitionFileDto {

    private Long id;

    @NotNull
    private ExhibitionFile.MediaType mediaType;

    private ExhibitionFile.FileType fileType;

    @NotBlank
    private String fileUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String fileSrcset;

    private String thumbnailUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String thumbnailSrcset;

    private String fileName;

    private String mimeType;

    private Long fileSize;

    private String title;

    private String titleEn;

    private String description;

    private String descriptionEn;

    private String source;

    private LocalDate publishedOn;

    private Integer displayOrder;
}
