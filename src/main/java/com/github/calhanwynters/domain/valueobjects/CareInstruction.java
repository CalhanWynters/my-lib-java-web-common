package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.exceptions.DomainRuleViolationException;
import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Hardened Value Object for product care instructions.
 * Aligned with DomainGuard for 2026 Edition (Java 21/25).
 */
public record CareInstruction(String instructions) {

    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 500;
    private static final double SAFETY_FACTOR = 1.5;

    /**
     * The "Null Object" constant.
     * Satisfies: NotBlank, MinLength (5), and Semantic Style (Hyphen).
     */
    public static final CareInstruction NONE = new CareInstruction("- N/A");


    // Prefix Patterns for Semantic Consistency
    private static final Pattern HYPHEN_PREFIX = Pattern.compile("^-");
    private static final Pattern ASTERISK_PREFIX = Pattern.compile("^\\*");
    private static final Pattern BULLET_DOT_PREFIX = Pattern.compile("^•");
    private static final Pattern NUMBER_PREFIX = Pattern.compile("^(?:[1-9]|1[0-5])\\.");

    /**
     * Unicode-aware content whitelist (Degree sign, symbols, and punctuation included).
     */
    private static final Pattern VALID_CONTENT_PATTERN =
            Pattern.compile("^(?U)[\\p{L}\\p{N} °.,:!\\n\\r*•()\\[\\]\\-?]+$");

    /**
     * Compact Constructor enforcing semantic list styles and lexical safety.
     */
    public CareInstruction {
        // 1. Existence (Throws VAL-010)
        DomainGuard.notBlank(instructions, "Care Instructions");

        // 2. Normalization
        instructions = instructions.replace("\r\n", "\n").replace("\r", "\n").strip();

        // 3. DoS Mitigation & Size (Throws VAL-014 and VAL-002)
        DomainGuard.ensure(
                instructions.length() <= (MAX_LENGTH * SAFETY_FACTOR),
                "Input raw data exceeds safety buffer.",
                "VAL-014", "DOS_PREVENTION"
        );

        DomainGuard.lengthBetween(instructions, MIN_LENGTH, MAX_LENGTH, "Care Instructions");

        // 4. Lexical Content (Throws VAL-004)
        DomainGuard.matches(instructions, VALID_CONTENT_PATTERN, "Care Instructions");

        // 5. Semantic Style Consistency (Bullets/Numbering)
        validateSemantics(instructions);
    }

    /**
     * Ensures all lines follow the same prefix style established in line 1.
     */
    private static void validateSemantics(String text) {
        String[] lines = text.split("\\R");
        if (lines.length == 0) return;

        // Determine the style from line 1
        Pattern style = Stream.of(HYPHEN_PREFIX, ASTERISK_PREFIX, BULLET_DOT_PREFIX, NUMBER_PREFIX)
                .filter(p -> p.matcher(lines[0].strip()).find())
                .findFirst()
                .orElseThrow(() -> new DomainRuleViolationException(
                        "Instructions must start with a valid bullet (-, *, •) or a number (1-15).",
                        "VAL-011",
                        "SEMANTICS"
                ));

        // Ensure subsequent lines follow the same style
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].strip();
            if (!line.isEmpty()) {
                DomainGuard.ensure(
                        style.matcher(line).find(),
                        "Prefix style mismatch: Line %d does not match established style.".formatted(i + 1),
                        "VAL-011", "SEMANTICS"
                );
            }
        }
    }


    /**
     * Domain logic check for existence.
     */
    public boolean isNone() {
        return this.equals(NONE);
    }
}
