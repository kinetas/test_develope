package com.trpg.dto.share;

import com.trpg.model.ContentType;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 목록 표시용 공유 콘텐츠 요약 DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedContentSummary {

    private Long id;
    private String title;
    private ContentType contentType;
    private String authorUsername;
    private int downloadCount;
    private int likeCount;
    private LocalDateTime createdAt;
}
