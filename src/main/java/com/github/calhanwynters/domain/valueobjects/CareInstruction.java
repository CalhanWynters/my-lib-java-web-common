package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.exceptions.DomainRuleViolationException;
import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Hardened Value Object for product care instructions.
 * Aligned with DomainGuard for 2026 Edition.
 */
public record CareInstruction(String instructions) {

    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 500;
    private static final double SAFETY_FACTOR = 1.5;

    public static final CareInstruction NONE = new CareInstruction("- N/A");

    // Prefix Patterns for Semantic Consistency
    private static final Pattern HYPHEN_PREFIX = Pattern.compile("^-");
    private static final Pattern ASTERISK_PREFIX = Pattern.compile("^\\*");
    private static final Pattern BULLET_DOT_PREFIX = Pattern.compile("^•");
    private static final Pattern NUMBER_PREFIX = Pattern.compile("^(?:[1-9]|1[0-5])\\.");

    // Unicode-aware content whitelist (Lexical/Syntax)
    private static final Pattern VALID_CONTENT_PATTERN =
            Pattern.compile("^(?U)[\\p{L}\\p{N} °.,:!\\n\\r*•()\\[\\]\\-?]+$");

    // Simple Lexical Filter (Ideally injected via a service, but defined here for invariant safety)
    private static final Predicate<String> PROFANITY_FILTER =
            s -> s.toLowerCase().contains("badword"); // Placeholder for your filter logic

    /**
     * Compact Constructor enforcing hierarchical domain guards.
     */
    public CareInstruction {
        // 1. EXISTENCE
        DomainGuard.notBlank(instructions, "Care Instructions");

        // 2. NORMALIZATION (Pre-validation prep)
        instructions = instructions.replace("\r\n", "\n").replace("\r", "\n").strip();

        // 3. SIZE & DOS MITIGATION
        DomainGuard.ensure(
                instructions.length() <= (MAX_LENGTH * SAFETY_FACTOR),
                "Input raw data exceeds safety buffer.",
                "VAL-014", "DOS_PREVENTION"
        );
        DomainGuard.lengthBetween(instructions, MIN_LENGTH, MAX_LENGTH, "Care Instructions");

        // 4. LEXICAL: Content Integrity (New)
        DomainGuard.noProfanity(instructions, PROFANITY_FILTER, "Care Instructions");

        // 5. SYNTAX: Regex Pattern Matching
        DomainGuard.matches(instructions, VALID_CONTENT_PATTERN, "Care Instructions");

        // 6. SEMANTICS: Style Consistency
        validateSemantics(instructions);
    }

    private static void validateSemantics(String text) {
        String[] lines = text.split("\\R");
        if (lines.length == 0) return;

        Pattern style = Stream.of(HYPHEN_PREFIX, ASTERISK_PREFIX, BULLET_DOT_PREFIX, NUMBER_PREFIX)
                .filter(p -> p.matcher(lines[0].strip()).find())
                .findFirst()
                .orElseThrow(() -> new DomainRuleViolationException(
                        "Instructions must start with a valid bullet (-, *, •) or a number (1-15).",
                        "VAL-011",
                        "SEMANTICS"
                ));

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

    public boolean isNone() {
        return this.equals(NONE);
    }
}
