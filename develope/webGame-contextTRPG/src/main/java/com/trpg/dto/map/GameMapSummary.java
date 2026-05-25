package com.trpg.dto.map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameMapSummary {

    private Long id;
    private String title;
    private int width;
    private int height;
    private String ownerUsername;
    private boolean isPublic;
    private LocalDateTime updatedAt;
}
