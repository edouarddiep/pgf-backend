package com.pgf.dto;

import lombok.Data;
import java.util.List;

@Data
public class ArchiveDto {
    private Long id;
    private String title;
    private Integer year;
    private String description;
    private String thumbnailUrl;
    private Integer displayOrder;
    private List<ArchiveFileDto> files;
}