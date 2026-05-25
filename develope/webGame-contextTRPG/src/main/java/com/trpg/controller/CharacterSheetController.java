package com.trpg.controller;

import com.trpg.dto.character.CharacterSheetDTO;
import com.trpg.dto.character.CharacterSheetRequest;
import com.trpg.model.User;
import com.trpg.repository.UserRepository;
import com.trpg.service.CharacterSheetService;
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
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterSheetController {

    private final CharacterSheetService characterSheetService;
    private final UserRepository userRepository;

    /**
     * POST /api/characters
     * 캐릭터 시트 생성.
     */
    @PostMapping
    public ResponseEntity<CharacterSheetDTO> create(
            @RequestBody CharacterSheetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User owner = resolveUser(userDetails);
        CharacterSheetDTO created = characterSheetService.create(request, owner);
        log.info("캐릭터 시트 생성 요청 완료: id={}, user={}", created.getId(), owner.getUsername());
        return ResponseEntity
                .created(URI.create("/api/characters/" + created.getId()))
                .body(created);
    }

    /**
     * GET /api/characters/my
     * 내 캐릭터 시트 목록 조회.
     */
    @GetMapping("/my")
    public ResponseEntity<List<CharacterSheetDTO>> getMySheets(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        List<CharacterSheetDTO> sheets = characterSheetService.getMySheets(user);
        return ResponseEntity.ok(sheets);
    }

    /**
     * GET /api/characters/public
     * 공개 캐릭터 시트 목록 조회.
     */
    @GetMapping("/public")
    public ResponseEntity<List<CharacterSheetDTO>> getPublicSheets() {
        List<CharacterSheetDTO> sheets = characterSheetService.getPublicSheets();
        return ResponseEntity.ok(sheets);
    }

    /**
     * GET /api/characters/{id}
     * 특정 캐릭터 시트 조회. 공개 시트는 인증 없이도 조회 가능, 비공개는 소유자만.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CharacterSheetDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userDetails != null ? resolveUserOptional(userDetails) : null;
        CharacterSheetDTO sheet = characterSheetService.getById(id, user);
        return ResponseEntity.ok(sheet);
    }

    /**
     * PUT /api/characters/{id}
     * 캐릭터 시트 수정. 소유자만 가능.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CharacterSheetDTO> update(
            @PathVariable Long id,
            @RequestBody CharacterSheetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        CharacterSheetDTO updated = characterSheetService.update(id, request, user);
        log.info("캐릭터 시트 수정 요청 완료: id={}, user={}", id, user.getUsername());
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/characters/{id}
     * 캐릭터 시트 삭제. 소유자만 가능.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        characterSheetService.delete(id, user);
        log.info("캐릭터 시트 삭제 요청 완료: id={}, user={}", id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new SecurityException("인증 정보가 없습니다.");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userDetails.getUsername()));
    }

    private User resolveUserOptional(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }
}
