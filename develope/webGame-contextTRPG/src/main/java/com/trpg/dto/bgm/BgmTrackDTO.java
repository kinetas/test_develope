package com.trpg.dto.bgm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BgmTrackDTO {

    private Long id;
    private String title;
    private Long audioFileId;
    private String audioFileName;
    private String ownerUsername;
    private boolean isPublic;
}
