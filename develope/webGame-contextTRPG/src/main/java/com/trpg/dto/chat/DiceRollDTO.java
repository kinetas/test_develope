package com.trpg.dto.chat;

import lombok.*;

/**
 * Transfer object for a dice-roll result broadcast.
 *
 * <p>Example payload:
 * <pre>
 * { "diceType": "d20", "result": 17, "roller": "alice" }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiceRollDTO {

    /**
     * Dice notation string, e.g. {@code "d4"}, {@code "d6"}, {@code "d8"},
     * {@code "d10"}, {@code "d12"}, {@code "d20"}.
     */
    private String diceType;

    /** The random value rolled (1 to N inclusive). */
    private int result;

    /** Username of the player who rolled the dice. */
    private String roller;
}
