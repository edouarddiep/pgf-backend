package com.pgf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pgf.model.ArchiveFile;
import lombok.Data;

@Data
public class ArchiveFileDto {
    private Long id;
    private ArchiveFile.FileType fileType;
    private String fileUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String fileSrcset;

    private String fileName;
}