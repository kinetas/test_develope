package com.trpg.controller;

import com.trpg.dto.chat.ChatMessageDTO;
import com.trpg.dto.chat.DiceRollDTO;
import com.trpg.model.MessageType;
import com.trpg.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles STOMP messages and REST endpoints for the chat feature.
 *
 * <h3>STOMP message flow</h3>
 * <pre>
 *  Client sends to /app/chat.send  → handler persists + broadcasts to /topic/session/{sessionId}
 *  Client sends to /app/chat.join  → broadcasts join notice
 *  Client sends to /app/chat.leave → broadcasts leave notice
 *  Client sends to /app/dice.roll  → rolls dice + broadcasts result
 * </pre>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── View ─────────────────────────────────────────────────────────────────

    /**
     * Serve the chat UI page.
     */
    @GetMapping("/chat")
    public String chatPage(Model model) {
        return "chat";
    }

    // ── STOMP message handlers ────────────────────────────────────────────────

    /**
     * Handle a regular chat message.
     *
     * <p>Client destination: {@code /app/chat.send}<br>
     * Broadcast destination: {@code /topic/session/{sessionId}}
     */
    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload ChatMessageDTO dto, Principal principal) {
        // Enforce sender from authenticated principal when available
        if (principal != null) {
            dto.setSender(principal.getName());
        }
        dto.setMessageType(MessageType.CHAT);
        dto.setSentAt(LocalDateTime.now());

        chatService.saveMessage(dto);

        String destination = "/topic/session/" + dto.getSessionId();
        messagingTemplate.convertAndSend(destination, dto);
        log.debug("Chat message from {} to session {}", dto.getSender(), dto.getSessionId());
    }

    /**
     * Handle a user joining a session.
     *
     * <p>Client destination: {@code /app/chat.join}<br>
     * Broadcast destination: {@code /topic/session/{sessionId}}
     */
    @MessageMapping("/chat.join")
    public void handleJoin(@Payload ChatMessageDTO dto, Principal principal) {
        if (principal != null) {
            dto.setSender(principal.getName());
        }
        dto.setMessageType(MessageType.JOIN);
        dto.setSentAt(LocalDateTime.now());
        dto.setContent(dto.getSender() + " 님이 입장하셨습니다.");

        chatService.saveMessage(dto);

        String destination = "/topic/session/" + dto.getSessionId();
        messagingTemplate.convertAndSend(destination, dto);
        log.info("User {} joined session {}", dto.getSender(), dto.getSessionId());
    }

    /**
     * Handle a user leaving a session.
     *
     * <p>Client destination: {@code /app/chat.leave}<br>
     * Broadcast destination: {@code /topic/session/{sessionId}}
     */
    @MessageMapping("/chat.leave")
    public void handleLeave(@Payload ChatMessageDTO dto, Principal principal) {
        if (principal != null) {
            dto.setSender(principal.getName());
        }
        dto.setMessageType(MessageType.LEAVE);
        dto.setSentAt(LocalDateTime.now());
        dto.setContent(dto.getSender() + " 님이 퇴장하셨습니다.");

        chatService.saveMessage(dto);

        String destination = "/topic/session/" + dto.getSessionId();
        messagingTemplate.convertAndSend(destination, dto);
        log.info("User {} left session {}", dto.getSender(), dto.getSessionId());
    }

    /**
     * Handle a dice-roll request.
     *
     * <p>Client destination: {@code /app/dice.roll}<br>
     * Broadcast destination: {@code /topic/session/{sessionId}}
     *
     * <p>The payload should include {@code sessionId} and {@code content} set to
     * the dice type (e.g. {@code "d20"}).
     */
    @MessageMapping("/dice.roll")
    public void handleDiceRoll(@Payload ChatMessageDTO dto, Principal principal) {
        String username = principal != null ? principal.getName() : dto.getSender();
        String diceType = dto.getContent();  // e.g. "d20"

        DiceRollDTO rollResult = chatService.rollDice(diceType, username);

        // Build a chat message describing the roll
        String content = String.format("[DICE] %s 이(가) %s 을(를) 굴려서 %d 이(가) 나왔습니다!",
                username, diceType, rollResult.getResult());

        ChatMessageDTO resultMsg = ChatMessageDTO.builder()
                .sessionId(dto.getSessionId())
                .sender(username)
                .content(content)
                .messageType(MessageType.DICE)
                .sentAt(LocalDateTime.now())
                .build();

        chatService.saveMessage(resultMsg);

        String destination = "/topic/session/" + dto.getSessionId();
        messagingTemplate.convertAndSend(destination, resultMsg);

        // Also send the structured DiceRollDTO for richer clients
        messagingTemplate.convertAndSend(destination + "/dice", rollResult);

        log.debug("Dice roll: {} rolled {} on {}, result: {}",
                username, rollResult.getResult(), diceType, dto.getSessionId());
    }

    // ── REST endpoints ────────────────────────────────────────────────────────

    /**
     * Retrieve the full chat history for a session.
     *
     * <pre>GET /api/chat/history/{sessionId}</pre>
     */
    @GetMapping("/api/chat/history/{sessionId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @PathVariable String sessionId) {
        List<ChatMessageDTO> history = chatService.getSessionHistory(sessionId);
        return ResponseEntity.ok(history);
    }
}
