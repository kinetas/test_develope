package com.trpg.repository;

import com.trpg.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data-access layer for {@link ChatMessage}.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Full history for a session, ordered chronologically.
     *
     * @param sessionId target session ID
     * @return ordered list of messages
     */
    List<ChatMessage> findBySessionIdOrderBySentAtAsc(String sessionId);

    /**
     * Messages in a session sent after a given timestamp (useful for
     * incremental polls or replay-from-point).
     *
     * @param sessionId target session ID
     * @param sentAt    exclusive lower bound
     * @return messages sent strictly after {@code sentAt}, ordered ascending
     */
    List<ChatMessage> findBySessionIdAndSentAtAfterOrderBySentAtAsc(
            String sessionId, LocalDateTime sentAt);
}
