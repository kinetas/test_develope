package com.trpg.repository;

import com.trpg.model.FileType;
import com.trpg.model.UploadedFile;
import com.trpg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findByUploader(User uploader);

    List<UploadedFile> findByFileType(FileType fileType);
}
