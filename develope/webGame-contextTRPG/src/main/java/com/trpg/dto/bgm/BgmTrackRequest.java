package com.trpg.dto.bgm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BgmTrackRequest {

    private String title;
    private Long audioFileId;
    private boolean isPublic;
}
