package com.trpg.controller;

import com.trpg.dto.share.SharedContentDTO;
import com.trpg.dto.share.SharedContentRequest;
import com.trpg.dto.share.SharedContentSummary;
import com.trpg.model.ContentType;
import com.trpg.model.User;
import com.trpg.repository.UserRepository;
import com.trpg.service.SharedContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class SharedContentController {

    private final SharedContentService sharedContentService;
    private final UserRepository userRepository;

    /**
     * POST /api/share
     * 공유 콘텐츠 등록.
     */
    @PostMapping
    public ResponseEntity<SharedContentDTO> create(
            @RequestBody SharedContentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User author = resolveUser(userDetails);
        SharedContentDTO created = sharedContentService.create(request, author);
        log.info("공유 콘텐츠 등록 완료: id={}, user={}", created.getId(), author.getUsername());
        return ResponseEntity
                .created(URI.create("/api/share/" + created.getId()))
                .body(created);
    }

    /**
     * GET /api/share
     * 전체 목록 조회. contentType 쿼리 파라미터가 있으면 해당 타입만 반환.
     */
    @GetMapping
    public ResponseEntity<List<SharedContentSummary>> getAll(
            @RequestParam(required = false) ContentType contentType) {

        List<SharedContentSummary> list = (contentType != null)
                ? sharedContentService.getByType(contentType)
                : sharedContentService.getAll();
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/share/popular
     * 인기 콘텐츠 목록 (다운로드 수 기준 상위 10개).
     */
    @GetMapping("/popular")
    public ResponseEntity<List<SharedContentSummary>> getPopular() {
        return ResponseEntity.ok(sharedContentService.getPopular());
    }

    /**
     * GET /api/share/my
     * 내가 등록한 공유 콘텐츠 목록.
     */
    @GetMapping("/my")
    public ResponseEntity<List<SharedContentSummary>> getMy(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        return ResponseEntity.ok(sharedContentService.getMyContent(user));
    }

    /**
     * GET /api/share/{id}
     * 공유 콘텐츠 상세 조회. downloadCount 증가.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SharedContentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sharedContentService.getById(id));
    }

    /**
     * DELETE /api/share/{id}
     * 공유 콘텐츠 삭제. 작성자만 가능.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        sharedContentService.delete(id, user);
        log.info("공유 콘텐츠 삭제 완료: id={}, user={}", id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/share/{id}/like
     * 좋아요 추가.
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id) {
        sharedContentService.like(id);
        return ResponseEntity.ok().build();
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new SecurityException("인증 정보가 없습니다.");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userDetails.getUsername()));
    }
}
