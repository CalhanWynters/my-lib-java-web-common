package com.github.calhanwynters.domain.common.valueobjects;

import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

import java.util.regex.Pattern;

/**
 * Hardened Value Object for Domain Names.
 * Aligned with DomainGuard for 2026 Edition (Java 21/25).
 */
public record Name(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;
    private static final int SAFETY_BUFFER = MAX_LENGTH + 10;

    /**
     * Lexical Whitelist (Unicode-aware):
     * Allows letters, numbers, and basic punctuation across global alphabets.
     */
    private static final Pattern ALLOWED_CHARS_PATTERN =
            Pattern.compile("^[\\p{L}\\p{N} .,:;!\\-?'\"()]+$");

    public static Name from(String value) {
        return new Name(value);
    }

    public Name {
        // 1. Initial Existence (Throws VAL-010)
        DomainGuard.notBlank(value, "name");

        // 2. DoS Mitigation
        DomainGuard.ensure(
                value.length() <= SAFETY_BUFFER,
                "Input exceeds safety buffer limits.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 3. Normalization
        String normalized = value.strip().replaceAll("\\s{2,}", " ");

        // 4. Invariant Validation (Throws VAL-002 and VAL-004)
        DomainGuard.lengthBetween(normalized, MIN_LENGTH, MAX_LENGTH, "name");
        DomainGuard.matches(normalized, ALLOWED_CHARS_PATTERN, "name");

        // 5. Parameter Reassignment for Record initialization
        value = normalized;
    }
}
