package com.trpg.service;

import com.trpg.dto.bgm.BgmTrackDTO;
import com.trpg.dto.bgm.BgmTrackRequest;
import com.trpg.model.BgmTrack;
import com.trpg.model.FileType;
import com.trpg.model.UploadedFile;
import com.trpg.model.User;
import com.trpg.repository.BgmTrackRepository;
import com.trpg.repository.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BgmService {

    private final BgmTrackRepository bgmTrackRepository;
    private final UploadedFileRepository uploadedFileRepository;

    /**
     * BGM 트랙을 등록합니다.
     *
     * @param request 트랙 등록 요청 DTO (title, audioFileId, isPublic)
     * @param owner   트랙 소유자
     * @return 등록된 트랙 DTO
     */
    @Transactional
    public BgmTrackDTO addTrack(BgmTrackRequest request, User owner) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("트랙 제목은 필수입니다.");
        }
        if (request.getAudioFileId() == null) {
            throw new IllegalArgumentException("오디오 파일 ID는 필수입니다.");
        }

        UploadedFile audioFile = uploadedFileRepository.findById(request.getAudioFileId())
                .orElseThrow(() -> new RuntimeException(
                        "업로드된 파일을 찾을 수 없습니다. id=" + request.getAudioFileId()));

        if (audioFile.getFileType() != FileType.BGM) {
            throw new IllegalArgumentException("BGM 타입의 파일만 트랙으로 등록할 수 있습니다.");
        }

        boolean isFileOwner = audioFile.getUploader().getId().equals(owner.getId());
        if (!isFileOwner) {
            throw new SecurityException("본인이 업로드한 파일만 트랙으로 등록할 수 있습니다.");
        }

        BgmTrack track = BgmTrack.builder()
                .title(request.getTitle().trim())
                .audioFile(audioFile)
                .owner(owner)
                .isPublic(request.isPublic())
                .build();

        BgmTrack saved = bgmTrackRepository.save(track);
        return toDTO(saved);
    }

    /**
     * BGM 트랙을 삭제합니다. 소유자만 삭제할 수 있습니다.
     *
     * @param id        삭제할 트랙 ID
     * @param requester 삭제 요청자
     */
    @Transactional
    public void deleteTrack(Long id, User requester) {
        BgmTrack track = bgmTrackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("트랙을 찾을 수 없습니다. id=" + id));

        if (!track.getOwner().getId().equals(requester.getId())) {
            throw new SecurityException("트랙 삭제 권한이 없습니다.");
        }

        bgmTrackRepository.delete(track);
    }

    /**
     * 내 BGM 트랙 목록을 조회합니다.
     *
     * @param owner 조회 대상 사용자
     * @return 트랙 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<BgmTrackDTO> getMyTracks(User owner) {
        return bgmTrackRepository.findByOwner(owner)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 공개된 BGM 트랙 목록을 조회합니다.
     *
     * @return 공개 트랙 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<BgmTrackDTO> getPublicTracks() {
        return bgmTrackRepository.findByIsPublicTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private BgmTrackDTO toDTO(BgmTrack track) {
        return BgmTrackDTO.builder()
                .id(track.getId())
                .title(track.getTitle())
                .audioFileId(track.getAudioFile().getId())
                .audioFileName(track.getAudioFile().getOriginalName())
                .ownerUsername(track.getOwner().getUsername())
                .isPublic(track.isPublic())
                .build();
    }
}
