package com.trpg.dto.share;

import com.trpg.model.ContentType;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 공유 콘텐츠 전체 응답 DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedContentDTO {

    private Long id;
    private String title;
    private String description;
    private String authorUsername;
    private ContentType contentType;
    private String linkedSheets;
    private String linkedMaps;
    private int downloadCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
