package com.trpg.service;

import com.trpg.dto.chat.ChatMessageDTO;
import com.trpg.dto.chat.DiceRollDTO;
import com.trpg.model.ChatMessage;
import com.trpg.model.MessageType;
import com.trpg.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Business logic for chat messages and dice rolls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    // Cryptographically weak but sufficient for game dice rolls.
    private final Random random = new Random();

    // ── Message persistence ───────────────────────────────────────────────────

    /**
     * Persist a chat message and return the saved entity mapped back to DTO.
     *
     * @param dto inbound message (sentAt may be null; will be set to now)
     * @return saved entity
     */
    @Transactional
    public ChatMessage saveMessage(ChatMessageDTO dto) {
        ChatMessage entity = ChatMessage.builder()
                .sessionId(dto.getSessionId())
                .sender(dto.getSender())
                .content(dto.getContent())
                .messageType(dto.getMessageType() != null ? dto.getMessageType() : MessageType.CHAT)
                .sentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(entity);
        log.debug("Saved chat message id={} session={} sender={}", saved.getId(), saved.getSessionId(), saved.getSender());
        return saved;
    }

    // ── History retrieval ─────────────────────────────────────────────────────

    /**
     * Retrieve the full ordered history for a session.
     *
     * @param sessionId target session
     * @return list of DTOs ordered by sentAt ascending
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getSessionHistory(String sessionId) {
        return chatMessageRepository
                .findBySessionIdOrderBySentAtAsc(sessionId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve messages sent after a given timestamp (for incremental load).
     *
     * @param sessionId target session
     * @param after     exclusive lower bound
     * @return list of DTOs after the given timestamp
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getSessionHistoryAfter(String sessionId, LocalDateTime after) {
        return chatMessageRepository
                .findBySessionIdAndSentAtAfterOrderBySentAtAsc(sessionId, after)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Dice rolling ──────────────────────────────────────────────────────────

    /**
     * Roll a dice of the specified type and return the result.
     *
     * <p>Supported types: {@code d4}, {@code d6}, {@code d8}, {@code d10},
     * {@code d12}, {@code d20}, {@code d100}.
     *
     * @param diceType e.g. {@code "d20"}
     * @param username the player performing the roll
     * @return a {@link DiceRollDTO} with the rolled value
     * @throws IllegalArgumentException for unknown dice types
     */
    public DiceRollDTO rollDice(String diceType, String username) {
        int sides = parseDiceSides(diceType);
        int result = random.nextInt(sides) + 1;  // 1 .. sides (inclusive)

        log.debug("Dice roll: {} rolled {} on {} (sides={})", username, result, diceType, sides);

        return DiceRollDTO.builder()
                .diceType(diceType)
                .result(result)
                .roller(username)
                .build();
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Parse the number of sides from a dice-type string such as {@code "d20"}.
     */
    private int parseDiceSides(String diceType) {
        if (diceType == null || diceType.isBlank()) {
            throw new IllegalArgumentException("diceType must not be blank");
        }
        String normalized = diceType.trim().toLowerCase();
        if (!normalized.startsWith("d")) {
            throw new IllegalArgumentException("Unknown dice type: " + diceType);
        }
        try {
            int sides = Integer.parseInt(normalized.substring(1));
            if (sides < 2) {
                throw new IllegalArgumentException("Dice must have at least 2 sides: " + diceType);
            }
            return sides;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse dice type: " + diceType, e);
        }
    }

    /** Map a {@link ChatMessage} entity to a {@link ChatMessageDTO}. */
    private ChatMessageDTO toDTO(ChatMessage entity) {
        return ChatMessageDTO.builder()
                .sessionId(entity.getSessionId())
                .sender(entity.getSender())
                .content(entity.getContent())
                .messageType(entity.getMessageType())
                .sentAt(entity.getSentAt())
                .build();
    }
}
