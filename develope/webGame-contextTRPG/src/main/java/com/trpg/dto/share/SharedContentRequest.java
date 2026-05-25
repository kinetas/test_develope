package com.trpg.dto.share;

import com.trpg.model.ContentType;
import lombok.*;

/**
 * 공유 콘텐츠 생성 요청 DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedContentRequest {

    private String title;
    private String description;
    private ContentType contentType;

    /** 연결된 CharacterSheet ID 목록 (JSON 배열 문자열, e.g. "[1,2,3]") */
    private String linkedSheets;

    /** 연결된 GameMap ID 목록 (JSON 배열 문자열, e.g. "[4,5]") */
    private String linkedMaps;
}
