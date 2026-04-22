package com.github.calhanwynters.domain.common.valueobjects;

import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

import java.util.regex.Pattern;

/**
 * Hardened Category Value Object for Java 21/25 (2026 Edition).
 * Implements Unicode-aware whitelisting and DoS prevention via DomainGuard.
 */
public record Category(String value) {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;
    private static final int SAFETY_BUFFER = MAX_LENGTH * 2;

    /**
     * Lexical Whitelist (Unicode-aware):
     * Allows letters, numbers, and spaces across global alphabets.
     */
    private static final Pattern VALID_CHARS = Pattern.compile("^[\\p{L}\\p{N} ]+$");

    /**
     * Compact Constructor enforcing category invariants.
     */
    public Category {
        // 1. Initial Existence (Throws VAL-010)
        DomainGuard.notBlank(value, "Category");

        // 2. DoS Mitigation (Throws VAL-014)
        DomainGuard.ensure(
                value.length() <= SAFETY_BUFFER,
                "Input raw data exceeds safety buffer limits.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 3. Normalization
        String normalized = value.strip().replaceAll("\\s{2,}", " ");

        // 4. Domain Invariant Validation (Throws VAL-002 and VAL-004)
        DomainGuard.lengthBetween(normalized, MIN_LENGTH, MAX_LENGTH, "Category");
        DomainGuard.matches(normalized, VALID_CHARS, "Category");

        // 5. Parameter Reassignment for Record initialization
        value = normalized;
    }
}
