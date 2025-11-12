package com.pgf.dto;

import com.pgf.model.ArchiveFile;
import lombok.Data;

@Data
public class ArchiveFileDto {
    private Long id;
    private ArchiveFile.FileType fileType;
    private String fileUrl;
    private String fileName;
    private Integer displayOrder;
}