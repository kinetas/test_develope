package com.trpg.dto.file;

import com.trpg.model.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private Long fileId;
    private String originalName;
    private FileType fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
