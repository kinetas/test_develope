package com.trpg.service;

import com.trpg.dto.share.SharedContentDTO;
import com.trpg.dto.share.SharedContentRequest;
import com.trpg.dto.share.SharedContentSummary;
import com.trpg.model.ContentType;
import com.trpg.model.SharedContent;
import com.trpg.model.User;
import com.trpg.repository.SharedContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharedContentService {

    private final SharedContentRepository sharedContentRepository;

    /**
     * 새 공유 콘텐츠를 등록합니다.
     *
     * @param request 등록 요청 데이터
     * @param author  등록 유저
     * @return 등록된 공유 콘텐츠 DTO
     */
    @Transactional
    public SharedContentDTO create(SharedContentRequest request, User author) {
        SharedContent content = SharedContent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(author)
                .contentType(request.getContentType())
                .linkedSheets(request.getLinkedSheets())
                .linkedMaps(request.getLinkedMaps())
                .build();

        SharedContent saved = sharedContentRepository.save(content);
        log.info("공유 콘텐츠 등록: id={}, title={}, author={}", saved.getId(), saved.getTitle(), author.getUsername());
        return toDTO(saved);
    }

    /**
     * 공유 콘텐츠를 삭제합니다. 작성자만 삭제 가능합니다.
     *
     * @param id   콘텐츠 ID
     * @param user 요청 유저
     */
    @Transactional
    public void delete(Long id, User user) {
        SharedContent content = sharedContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공유 콘텐츠를 찾을 수 없습니다. id=" + id));

        if (!content.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("해당 공유 콘텐츠를 삭제할 권한이 없습니다. id=" + id);
        }

        sharedContentRepository.delete(content);
        log.info("공유 콘텐츠 삭제: id={}, user={}", id, user.getUsername());
    }

    /**
     * 특정 공유 콘텐츠를 조회합니다. 조회 시 downloadCount를 1 증가시킵니다.
     *
     * @param id 콘텐츠 ID
     * @return 공유 콘텐츠 DTO
     */
    @Transactional
    public SharedContentDTO getById(Long id) {
        SharedContent content = sharedContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공유 콘텐츠를 찾을 수 없습니다. id=" + id));

        content.setDownloadCount(content.getDownloadCount() + 1);
        SharedContent updated = sharedContentRepository.save(content);
        log.debug("공유 콘텐츠 조회 (downloadCount 증가): id={}, count={}", id, updated.getDownloadCount());
        return toDTO(updated);
    }

    /**
     * 전체 공유 콘텐츠 목록을 최신순으로 조회합니다.
     *
     * @return 공유 콘텐츠 요약 목록
     */
    @Transactional(readOnly = true)
    public List<SharedContentSummary> getAll() {
        return sharedContentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * 특정 타입의 공유 콘텐츠 목록을 조회합니다.
     *
     * @param contentType 콘텐츠 타입
     * @return 공유 콘텐츠 요약 목록
     */
    @Transactional(readOnly = true)
    public List<SharedContentSummary> getByType(ContentType contentType) {
        return sharedContentRepository.findByContentType(contentType)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * 다운로드 수 기준 인기 공유 콘텐츠 목록(상위 10개)을 조회합니다.
     *
     * @return 공유 콘텐츠 요약 목록
     */
    @Transactional(readOnly = true)
    public List<SharedContentSummary> getPopular() {
        return sharedContentRepository.findTop10ByOrderByDownloadCountDesc()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * 내가 등록한 공유 콘텐츠 목록을 조회합니다.
     *
     * @param user 요청 유저
     * @return 공유 콘텐츠 요약 목록
     */
    @Transactional(readOnly = true)
    public List<SharedContentSummary> getMyContent(User user) {
        return sharedContentRepository.findByAuthor(user)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * 특정 공유 콘텐츠에 좋아요를 추가합니다.
     *
     * @param id 콘텐츠 ID
     */
    @Transactional
    public void like(Long id) {
        SharedContent content = sharedContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공유 콘텐츠를 찾을 수 없습니다. id=" + id));

        content.setLikeCount(content.getLikeCount() + 1);
        sharedContentRepository.save(content);
        log.debug("공유 콘텐츠 좋아요: id={}, likeCount={}", id, content.getLikeCount());
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private SharedContentDTO toDTO(SharedContent content) {
        return SharedContentDTO.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .authorUsername(content.getAuthor().getUsername())
                .contentType(content.getContentType())
                .linkedSheets(content.getLinkedSheets())
                .linkedMaps(content.getLinkedMaps())
                .downloadCount(content.getDownloadCount())
                .likeCount(content.getLikeCount())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }

    private SharedContentSummary toSummary(SharedContent content) {
        return SharedContentSummary.builder()
                .id(content.getId())
                .title(content.getTitle())
                .contentType(content.getContentType())
                .authorUsername(content.getAuthor().getUsername())
                .downloadCount(content.getDownloadCount())
                .likeCount(content.getLikeCount())
                .createdAt(content.getCreatedAt())
                .build();
    }
}
