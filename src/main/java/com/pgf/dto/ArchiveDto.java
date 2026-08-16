package com.pgf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ArchiveDto {
    private Long id;
    private String title;
    private String titleEn;
    private Integer year;
    private String description;
    private String descriptionEn;
    private String thumbnailUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String thumbnailSrcset;

    private Integer mainImagePositionX;
    private Integer mainImagePositionY;
    private Integer mainImageZoom;
    private List<ArchiveFileDto> files;
}