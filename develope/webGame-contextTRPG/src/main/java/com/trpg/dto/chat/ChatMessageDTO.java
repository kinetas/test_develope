package com.trpg.dto.chat;

import com.trpg.model.MessageType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Transfer object for chat messages travelling over STOMP and REST.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    /** Game session this message belongs to. */
    private String sessionId;

    /** Username of the sender. */
    private String sender;

    /** Message body text. */
    private String content;

    /** Classification of the message. */
    private MessageType messageType;

    /** Server-assigned send timestamp (null for inbound client messages). */
    private LocalDateTime sentAt;
}
