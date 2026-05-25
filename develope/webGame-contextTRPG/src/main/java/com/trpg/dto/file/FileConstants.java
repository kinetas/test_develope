package com.trpg.dto.file;

/**
 * 파일 업로드 관련 상수 정의.
 *
 * application.properties 추가 설정 예시:
 *   file.upload-dir=uploads/
 *   spring.servlet.multipart.max-file-size=50MB
 *   spring.servlet.multipart.max-request-size=55MB
 */
public final class FileConstants {

    private FileConstants() {
        // 유틸리티 클래스 — 인스턴스화 불가
    }

    /** 단일 파일 최대 허용 크기: 50 MB */
    public static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    /** 허용 확장자 목록 */
    public static final String[] ALLOWED_EXTENSIONS = {
        ".xlsx", ".xls",
        ".png", ".jpg", ".gif",
        ".mp3", ".wav", ".ogg",
        ".json", ".txt"
    };
}
