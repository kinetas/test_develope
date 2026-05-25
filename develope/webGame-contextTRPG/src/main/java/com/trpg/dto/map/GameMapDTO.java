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
public class GameMapDTO {

    private Long id;
    private String title;
    private String mapData;
    private Long backgroundImageId;
    private String backgroundImageName;
    private int width;
    private int height;
    private boolean isPublic;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
