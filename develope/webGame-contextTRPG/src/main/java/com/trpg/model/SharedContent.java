package com.trpg.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 커뮤니티에 공유된 TRPG 패키지(룰셋, 캐릭터 시트 양식, 맵 등) 엔티티.
 */
@Entity
@Table(name = "shared_contents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 공유 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 공유 설명 */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 공유 등록 유저 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** 콘텐츠 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private ContentType contentType;

    /** 연결된 CharacterSheet ID 목록 (JSON 배열 문자열) */
    @Column(name = "linked_sheets", columnDefinition = "TEXT")
    private String linkedSheets;

    /** 연결된 GameMap ID 목록 (JSON 배열 문자열) */
    @Column(name = "linked_maps", columnDefinition = "TEXT")
    private String linkedMaps;

    /** 다운로드 횟수 */
    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private int downloadCount = 0;

    /** 좋아요 수 */
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private int likeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
