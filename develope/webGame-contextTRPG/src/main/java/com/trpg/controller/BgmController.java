package com.trpg.controller;

import com.trpg.dto.bgm.BgmTrackDTO;
import com.trpg.dto.bgm.BgmTrackRequest;
import com.trpg.model.User;
import com.trpg.repository.UserRepository;
import com.trpg.service.BgmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bgm")
@RequiredArgsConstructor
public class BgmController {

    private final BgmService bgmService;
    private final UserRepository userRepository;

    /**
     * POST /api/bgm
     * BGM 트랙 등록. audioFileId로 기존 업로드 파일을 참조합니다.
     */
    @PostMapping
    public ResponseEntity<BgmTrackDTO> addTrack(
            @RequestBody BgmTrackRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User owner = resolveUser(userDetails);
        BgmTrackDTO dto = bgmService.addTrack(request, owner);

        log.info("BGM 트랙 등록 완료: id={}, title={}, owner={}",
                dto.getId(), dto.getTitle(), owner.getUsername());

        return ResponseEntity.ok(dto);
    }

    /**
     * DELETE /api/bgm/{id}
     * BGM 트랙 삭제. 소유자만 삭제할 수 있습니다.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User requester = resolveUser(userDetails);
        bgmService.deleteTrack(id, requester);

        log.info("BGM 트랙 삭제 완료: id={}, requester={}", id, requester.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/bgm/my
     * 내 BGM 트랙 목록 조회.
     */
    @GetMapping("/my")
    public ResponseEntity<List<BgmTrackDTO>> getMyTracks(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = resolveUser(userDetails);
        List<BgmTrackDTO> tracks = bgmService.getMyTracks(currentUser);
        return ResponseEntity.ok(tracks);
    }

    /**
     * GET /api/bgm/public
     * 공개 BGM 트랙 목록 조회.
     */
    @GetMapping("/public")
    public ResponseEntity<List<BgmTrackDTO>> getPublicTracks() {
        List<BgmTrackDTO> tracks = bgmService.getPublicTracks();
        return ResponseEntity.ok(tracks);
    }

    // ── private helper ────────────────────────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new SecurityException("인증 정보가 없습니다.");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(
                        "사용자를 찾을 수 없습니다: " + userDetails.getUsername()));
    }
}
