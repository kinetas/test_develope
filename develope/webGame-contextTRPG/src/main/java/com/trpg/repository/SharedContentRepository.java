package com.trpg.repository;

import com.trpg.model.ContentType;
import com.trpg.model.SharedContent;
import com.trpg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedContentRepository extends JpaRepository<SharedContent, Long> {

    /** 최신순 전체 목록 */
    List<SharedContent> findAllByOrderByCreatedAtDesc();

    /** 특정 유저의 공유 목록 */
    List<SharedContent> findByAuthor(User author);

    /** 특정 타입의 공유 목록 */
    List<SharedContent> findByContentType(ContentType contentType);

    /** 다운로드 수 기준 상위 10개 */
    List<SharedContent> findTop10ByOrderByDownloadCountDesc();
}
