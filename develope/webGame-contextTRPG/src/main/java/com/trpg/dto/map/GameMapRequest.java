package com.trpg.dto.map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameMapRequest {

    @NotBlank(message = "맵 이름은 필수입니다.")
    @Size(max = 100, message = "맵 이름은 100자 이하여야 합니다.")
    private String title;

    private String mapData;

    private Long backgroundImageId;

    @Min(value = 1, message = "가로 크기는 1 이상이어야 합니다.")
    private int width;

    @Min(value = 1, message = "세로 크기는 1 이상이어야 합니다.")
    private int height;

    private boolean isPublic;
}
