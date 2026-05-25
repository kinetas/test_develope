package com.trpg.controller;

import com.trpg.dto.map.GameMapDTO;
import com.trpg.dto.map.GameMapRequest;
import com.trpg.dto.map.GameMapSummary;
import com.trpg.model.User;
import com.trpg.repository.UserRepository;
import com.trpg.service.GameMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
public class GameMapController {

    private final GameMapService gameMapService;
    private final UserRepository userRepository;

    /**
     * POST /api/maps
     * 새 맵을 생성합니다.
     */
    @PostMapping
    public ResponseEntity<?> createMap(
            @Valid @RequestBody GameMapRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User owner = resolveUser(userDetails);
            GameMapDTO dto = gameMapService.create(request, owner);
            log.info("맵 생성 API 호출: id={}, owner={}", dto.getId(), owner.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/maps/my
     * 내 맵 목록을 조회합니다.
     */
    @GetMapping("/my")
    public ResponseEntity<List<GameMapSummary>> getMyMaps(
            @AuthenticationPrincipal UserDetails userDetails) {
        User owner = resolveUser(userDetails);
        List<GameMapSummary> maps = gameMapService.getMyMaps(owner);
        return ResponseEntity.ok(maps);
    }

    /**
     * GET /api/maps/public
     * 공개 맵 목록을 조회합니다.
     */
    @GetMapping("/public")
    public ResponseEntity<List<GameMapSummary>> getPublicMaps() {
        List<GameMapSummary> maps = gameMapService.getPublicMaps();
        return ResponseEntity.ok(maps);
    }

    /**
     * GET /api/maps/{id}
     * 특정 맵을 조회합니다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMap(@PathVariable Long id) {
        try {
            GameMapDTO dto = gameMapService.getById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/maps/{id}
     * 맵을 수정합니다. 소유자만 수정 가능합니다.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMap(
            @PathVariable Long id,
            @Valid @RequestBody GameMapRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User owner = resolveUser(userDetails);
            GameMapDTO dto = gameMapService.update(id, request, owner);
            log.info("맵 수정 API 호출: id={}, owner={}", id, owner.getUsername());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/maps/{id}
     * 맵을 삭제합니다. 소유자만 삭제 가능합니다.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMap(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User owner = resolveUser(userDetails);
            gameMapService.delete(id, owner);
            log.info("맵 삭제 API 호출: id={}, owner={}", id, owner.getUsername());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── private helper ────────────────────────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new SecurityException("인증 정보가 없습니다.");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userDetails.getUsername()));
    }
}
