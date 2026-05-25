package com.trpg.dto.character;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterSheetRequest {

    private String name;
    private String sheetData;
    private Long linkedFileId;
    private boolean isPublic;
}
