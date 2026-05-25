package com.trpg.service;

import com.trpg.dto.character.CharacterSheetDTO;
import com.trpg.dto.character.CharacterSheetRequest;
import com.trpg.model.CharacterSheet;
import com.trpg.model.UploadedFile;
import com.trpg.model.User;
import com.trpg.repository.CharacterSheetRepository;
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
public class CharacterSheetService {

    private final CharacterSheetRepository characterSheetRepository;
    private final UploadedFileRepository uploadedFileRepository;

    /**
     * 새 캐릭터 시트를 생성합니다.
     *
     * @param request 생성 요청 데이터
     * @param owner   소유 유저
     * @return 생성된 캐릭터 시트 DTO
     */
    @Transactional
    public CharacterSheetDTO create(CharacterSheetRequest request, User owner) {
        UploadedFile linkedFile = resolveLinkedFile(request.getLinkedFileId());

        CharacterSheet sheet = CharacterSheet.builder()
                .name(request.getName())
                .owner(owner)
                .sheetData(request.getSheetData())
                .linkedFile(linkedFile)
                .isPublic(request.isPublic())
                .build();

        CharacterSheet saved = characterSheetRepository.save(sheet);
        log.info("캐릭터 시트 생성: id={}, name={}, owner={}", saved.getId(), saved.getName(), owner.getUsername());
        return toDTO(saved);
    }

    /**
     * 캐릭터 시트를 수정합니다. 소유자만 수정 가능합니다.
     *
     * @param id      시트 ID
     * @param request 수정 요청 데이터
     * @param user    요청 유저
     * @return 수정된 캐릭터 시트 DTO
     */
    @Transactional
    public CharacterSheetDTO update(Long id, CharacterSheetRequest request, User user) {
        CharacterSheet sheet = characterSheetRepository.findByOwnerAndId(user, id)
                .orElseThrow(() -> new RuntimeException("캐릭터 시트를 찾을 수 없거나 수정 권한이 없습니다. id=" + id));

        UploadedFile linkedFile = resolveLinkedFile(request.getLinkedFileId());

        sheet.setName(request.getName());
        sheet.setSheetData(request.getSheetData());
        sheet.setLinkedFile(linkedFile);
        sheet.setPublic(request.isPublic());

        CharacterSheet updated = characterSheetRepository.save(sheet);
        log.info("캐릭터 시트 수정: id={}, name={}, owner={}", updated.getId(), updated.getName(), user.getUsername());
        return toDTO(updated);
    }

    /**
     * 캐릭터 시트를 삭제합니다. 소유자만 삭제 가능합니다.
     *
     * @param id   시트 ID
     * @param user 요청 유저
     */
    @Transactional
    public void delete(Long id, User user) {
        CharacterSheet sheet = characterSheetRepository.findByOwnerAndId(user, id)
                .orElseThrow(() -> new RuntimeException("캐릭터 시트를 찾을 수 없거나 삭제 권한이 없습니다. id=" + id));

        characterSheetRepository.delete(sheet);
        log.info("캐릭터 시트 삭제: id={}, owner={}", id, user.getUsername());
    }

    /**
     * 특정 ID의 캐릭터 시트를 조회합니다.
     * 공개 시트는 누구나, 비공개 시트는 소유자만 조회 가능합니다.
     *
     * @param id   시트 ID
     * @param user 요청 유저 (null 가능)
     * @return 캐릭터 시트 DTO
     */
    @Transactional(readOnly = true)
    public CharacterSheetDTO getById(Long id, User user) {
        CharacterSheet sheet = characterSheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("캐릭터 시트를 찾을 수 없습니다. id=" + id));

        boolean isOwner = user != null && sheet.getOwner().getId().equals(user.getId());
        if (!sheet.isPublic() && !isOwner) {
            throw new SecurityException("해당 캐릭터 시트에 접근 권한이 없습니다. id=" + id);
        }

        return toDTO(sheet);
    }

    /**
     * 내 캐릭터 시트 목록을 조회합니다.
     *
     * @param user 소유 유저
     * @return 캐릭터 시트 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<CharacterSheetDTO> getMySheets(User user) {
        return characterSheetRepository.findByOwner(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 공개된 캐릭터 시트 목록을 조회합니다.
     *
     * @return 공개 캐릭터 시트 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<CharacterSheetDTO> getPublicSheets() {
        return characterSheetRepository.findByIsPublicTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private UploadedFile resolveLinkedFile(Long fileId) {
        if (fileId == null) {
            return null;
        }
        return uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("연결된 파일을 찾을 수 없습니다. id=" + fileId));
    }

    private CharacterSheetDTO toDTO(CharacterSheet sheet) {
        return CharacterSheetDTO.builder()
                .id(sheet.getId())
                .name(sheet.getName())
                .sheetData(sheet.getSheetData())
                .linkedFileId(sheet.getLinkedFile() != null ? sheet.getLinkedFile().getId() : null)
                .isPublic(sheet.isPublic())
                .ownerUsername(sheet.getOwner().getUsername())
                .createdAt(sheet.getCreatedAt())
                .updatedAt(sheet.getUpdatedAt())
                .build();
    }
}
