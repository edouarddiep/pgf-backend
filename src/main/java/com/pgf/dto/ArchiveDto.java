package com.pgf.dto;

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
    private Integer mainImagePositionX;
    private Integer mainImagePositionY;
    private Integer mainImageZoom;
    private List<ArchiveFileDto> files;
}