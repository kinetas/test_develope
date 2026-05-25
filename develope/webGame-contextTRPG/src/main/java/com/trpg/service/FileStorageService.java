package com.trpg.service;

import com.trpg.config.FileStorageConfig;
import com.trpg.dto.file.FileConstants;
import com.trpg.model.FileType;
import com.trpg.model.UploadedFile;
import com.trpg.model.User;
import com.trpg.model.UserRole;
import com.trpg.repository.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;
    private final UploadedFileRepository uploadedFileRepository;

    /**
     * 파일을 저장하고 UploadedFile 엔티티를 반환합니다.
     *
     * @param multipartFile 업로드된 파일
     * @param fileType      파일 유형 (CHARACTER_SHEET, MAP, BGM, IMAGE, OTHER)
     * @param uploader      업로드 요청자
     * @return 저장된 UploadedFile 엔티티
     */
    @Transactional
    public UploadedFile storeFile(MultipartFile multipartFile, FileType fileType, User uploader) {
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        // 확장자 검증
        String extension = extractExtension(originalFilename).toLowerCase();
        boolean allowed = Arrays.stream(FileConstants.ALLOWED_EXTENSIONS)
                .anyMatch(ext -> ext.equalsIgnoreCase(extension));
        if (!allowed) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
        }

        // 파일 크기 검증
        if (multipartFile.getSize() > FileConstants.MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 50MB를 초과합니다.");
        }

        // UUID 기반 저장명 생성
        String storedName = UUID.randomUUID().toString() + extension;
        Path targetPath = fileStorageConfig.getUploadPath().resolve(storedName);

        try {
            Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + originalFilename, e);
        }

        UploadedFile uploadedFile = UploadedFile.builder()
                .originalName(originalFilename)
                .storedName(storedName)
                .filePath(targetPath.toString())
                .fileType(fileType)
                .fileSize(multipartFile.getSize())
                .uploader(uploader)
                .build();

        return uploadedFileRepository.save(uploadedFile);
    }

    /**
     * 저장명으로 파일 리소스를 로드합니다.
     *
     * @param storedName UUID 기반 저장명
     * @return 파일 Resource
     */
    public Resource loadFileAsResource(String storedName) {
        Path filePath = fileStorageConfig.getUploadPath().resolve(storedName).normalize();
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("파일을 읽을 수 없습니다: " + storedName);
        } catch (MalformedURLException e) {
            throw new RuntimeException("잘못된 파일 경로입니다: " + storedName, e);
        }
    }

    /**
     * 파일을 삭제합니다. 본인 또는 ADMIN만 삭제할 수 있습니다.
     *
     * @param fileId    삭제할 파일 ID
     * @param requester 삭제 요청자
     */
    @Transactional
    public void deleteFile(Long fileId, User requester) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. id=" + fileId));

        boolean isOwner = file.getUploader().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("파일 삭제 권한이 없습니다.");
        }

        // 실제 파일 삭제
        try {
            Path filePath = Path.of(file.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다: " + file.getStoredName(), e);
        }

        uploadedFileRepository.delete(file);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex);
    }
}
