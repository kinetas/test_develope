package com.trpg.dto.character;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterSheetDTO {

    private Long id;
    private String name;
    private String sheetData;
    private Long linkedFileId;
    private boolean isPublic;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
