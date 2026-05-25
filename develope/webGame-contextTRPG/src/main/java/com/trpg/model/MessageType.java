package com.trpg.model;

/**
 * STOMP chat message types.
 *
 * CHAT   — normal user message
 * JOIN   — user entered the session
 * LEAVE  — user left the session
 * DICE   — dice roll result
 * SYSTEM — server-generated notice
 */
public enum MessageType {
    CHAT,
    JOIN,
    LEAVE,
    DICE,
    SYSTEM
}
