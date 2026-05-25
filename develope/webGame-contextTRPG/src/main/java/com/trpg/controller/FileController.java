package com.trpg.controller;

import com.trpg.dto.file.FileUploadResponse;
import com.trpg.model.FileType;
import com.trpg.model.UploadedFile;
import com.trpg.model.User;
import com.trpg.repository.UploadedFileRepository;
import com.trpg.repository.UserRepository;
import com.trpg.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    /**
     * POST /api/files/upload
     * 파일 업로드 (multipart/form-data).
     * 파라미터: file (MultipartFile), fileType (FileType enum 이름)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileTypeStr,
            @AuthenticationPrincipal UserDetails userDetails) {

        User uploader = resolveUser(userDetails);

        FileType fileType;
        try {
            fileType = FileType.valueOf(fileTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 fileType입니다: " + fileTypeStr);
        }

        UploadedFile saved = fileStorageService.storeFile(file, fileType, uploader);

        FileUploadResponse response = FileUploadResponse.builder()
                .fileId(saved.getId())
                .originalName(saved.getOriginalName())
                .fileType(saved.getFileType())
                .fileSize(saved.getFileSize())
                .uploadedAt(saved.getUploadedAt())
                .build();

        log.info("파일 업로드 완료: id={}, name={}, uploader={}",
                saved.getId(), saved.getOriginalName(), uploader.getUsername());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/files/{fileId}
     * 파일 다운로드.
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 인증 확인 (SecurityConfig에서 이미 처리되나 명시적으로 재확인)
        resolveUser(userDetails);

        UploadedFile fileRecord = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. id=" + fileId));

        Resource resource = fileStorageService.loadFileAsResource(fileRecord.getStoredName());

        String contentDisposition = "attachment; filename=\"" + fileRecord.getOriginalName() + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentLength(fileRecord.getFileSize())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * DELETE /api/files/{fileId}
     * 파일 삭제. 본인 또는 ADMIN만 삭제 가능.
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User requester = resolveUser(userDetails);
        fileStorageService.deleteFile(fileId, requester);

        log.info("파일 삭제 완료: id={}, requester={}", fileId, requester.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/files/my
     * 내가 업로드한 파일 목록 조회.
     */
    @GetMapping("/my")
    public ResponseEntity<List<FileUploadResponse>> getMyFiles(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = resolveUser(userDetails);

        List<FileUploadResponse> files = uploadedFileRepository.findByUploader(currentUser)
                .stream()
                .map(f -> FileUploadResponse.builder()
                        .fileId(f.getId())
                        .originalName(f.getOriginalName())
                        .fileType(f.getFileType())
                        .fileSize(f.getFileSize())
                        .uploadedAt(f.getUploadedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(files);
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
