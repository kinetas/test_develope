package com.trpg.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Persisted chat message belonging to a game session.
 */
@Entity
@Table(name = "chat_messages",
       indexes = {
           @Index(name = "idx_chat_session_sent", columnList = "sessionId, sentAt")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The game session this message belongs to. */
    @Column(nullable = false, length = 100)
    private String sessionId;

    /** Username of the sender (maps to User.username). */
    @Column(nullable = false, length = 50)
    private String sender;

    /** Raw message text (or dice result description). */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Message classification. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    /** UTC timestamp when the message was created. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }
}
