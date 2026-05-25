package com.trpg.service;

import com.trpg.dto.map.GameMapDTO;
import com.trpg.dto.map.GameMapRequest;
import com.trpg.dto.map.GameMapSummary;
import com.trpg.model.GameMap;
import com.trpg.model.UploadedFile;
import com.trpg.model.User;
import com.trpg.repository.GameMapRepository;
import com.trpg.repository.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameMapService {

    private final GameMapRepository gameMapRepository;
    private final UploadedFileRepository uploadedFileRepository;

    /**
     * 새 맵을 생성합니다.
     *
     * @param request 맵 생성 요청 DTO
     * @param owner   소유자 User
     * @return 생성된 맵의 GameMapDTO
     */
    @Transactional
    public GameMapDTO create(GameMapRequest request, User owner) {
        UploadedFile backgroundImage = resolveBackgroundImage(request.getBackgroundImageId());

        GameMap map = GameMap.builder()
                .title(request.getTitle())
                .owner(owner)
                .mapData(request.getMapData())
                .backgroundImage(backgroundImage)
                .width(request.getWidth())
                .height(request.getHeight())
                .isPublic(request.isPublic())
                .build();

        GameMap saved = gameMapRepository.save(map);
        log.info("맵 생성 완료: id={}, title={}, owner={}", saved.getId(), saved.getTitle(), owner.getUsername());
        return toDTO(saved);
    }

    /**
     * 기존 맵을 수정합니다. 소유자만 수정 가능합니다.
     *
     * @param id      수정할 맵 ID
     * @param request 수정 요청 DTO
     * @param owner   요청자 User
     * @return 수정된 맵의 GameMapDTO
     */
    @Transactional
    public GameMapDTO update(Long id, GameMapRequest request, User owner) {
        GameMap map = gameMapRepository.findByOwnerAndId(owner, id)
                .orElseThrow(() -> new RuntimeException("맵을 찾을 수 없거나 수정 권한이 없습니다. id=" + id));

        UploadedFile backgroundImage = resolveBackgroundImage(request.getBackgroundImageId());

        map.setTitle(request.getTitle());
        map.setMapData(request.getMapData());
        map.setBackgroundImage(backgroundImage);
        map.setWidth(request.getWidth());
        map.setHeight(request.getHeight());
        map.setPublic(request.isPublic());

        GameMap saved = gameMapRepository.save(map);
        log.info("맵 수정 완료: id={}, title={}, owner={}", saved.getId(), saved.getTitle(), owner.getUsername());
        return toDTO(saved);
    }

    /**
     * 맵을 삭제합니다. 소유자만 삭제 가능합니다.
     *
     * @param id    삭제할 맵 ID
     * @param owner 요청자 User
     */
    @Transactional
    public void delete(Long id, User owner) {
        GameMap map = gameMapRepository.findByOwnerAndId(owner, id)
                .orElseThrow(() -> new RuntimeException("맵을 찾을 수 없거나 삭제 권한이 없습니다. id=" + id));

        gameMapRepository.delete(map);
        log.info("맵 삭제 완료: id={}, owner={}", id, owner.getUsername());
    }

    /**
     * ID로 맵을 조회합니다.
     *
     * @param id 조회할 맵 ID
     * @return GameMapDTO
     */
    @Transactional(readOnly = true)
    public GameMapDTO getById(Long id) {
        GameMap map = gameMapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("맵을 찾을 수 없습니다. id=" + id));
        return toDTO(map);
    }

    /**
     * 특정 유저의 맵 목록을 조회합니다.
     *
     * @param owner 소유자 User
     * @return GameMapSummary 목록
     */
    @Transactional(readOnly = true)
    public List<GameMapSummary> getMyMaps(User owner) {
        return gameMapRepository.findByOwner(owner)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * 공개 맵 목록을 조회합니다.
     *
     * @return 공개 GameMapSummary 목록
     */
    @Transactional(readOnly = true)
    public List<GameMapSummary> getPublicMaps() {
        return gameMapRepository.findByIsPublicTrue()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private UploadedFile resolveBackgroundImage(Long backgroundImageId) {
        if (backgroundImageId == null) {
            return null;
        }
        return uploadedFileRepository.findById(backgroundImageId)
                .orElseThrow(() -> new RuntimeException("배경 이미지 파일을 찾을 수 없습니다. id=" + backgroundImageId));
    }

    private GameMapDTO toDTO(GameMap map) {
        return GameMapDTO.builder()
                .id(map.getId())
                .title(map.getTitle())
                .mapData(map.getMapData())
                .backgroundImageId(map.getBackgroundImage() != null ? map.getBackgroundImage().getId() : null)
                .backgroundImageName(map.getBackgroundImage() != null ? map.getBackgroundImage().getOriginalName() : null)
                .width(map.getWidth())
                .height(map.getHeight())
                .isPublic(map.isPublic())
                .ownerUsername(map.getOwner().getUsername())
                .createdAt(map.getCreatedAt())
                .updatedAt(map.getUpdatedAt())
                .build();
    }

    private GameMapSummary toSummary(GameMap map) {
        return GameMapSummary.builder()
                .id(map.getId())
                .title(map.getTitle())
                .width(map.getWidth())
                .height(map.getHeight())
                .ownerUsername(map.getOwner().getUsername())
                .isPublic(map.isPublic())
                .updatedAt(map.getUpdatedAt())
                .build();
    }
}
