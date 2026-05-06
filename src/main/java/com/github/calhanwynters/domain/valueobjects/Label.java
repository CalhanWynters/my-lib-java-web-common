package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.util.regex.Pattern;

/**
 * Hardened Label Value Object for Java 21/25 (2026 Edition).
 * Enforces lexical standards and DoS boundaries via DomainGuard.
 */
public record Label(String value) {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;
    private static final int SAFETY_BUFFER = MAX_LENGTH * 2;

    /**
     * Lexical Whitelist:
     * Prevents leading/trailing spaces and ensures alphanumeric content.
     */
    private static final Pattern ALLOWED_CHARS_PATTERN =
            Pattern.compile("^[a-zA-Z0-9](?:[a-zA-Z0-9 -]*[a-zA-Z0-9])?$");

    /**
     * Static factory method for domain-driven instantiation.
     */
    public static Label from(String value) {
        return new Label(value);
    }

    /**
     * Compact constructor using DomainGuard for invariant enforcement.
     */
    public Label {
        // 1. Existence Check
        DomainGuard.notBlank(value, "Label Value");

        // 2. DOS Mitigation (Check raw length before heavy Regex)
        DomainGuard.ensure(
                value.length() <= SAFETY_BUFFER,
                "Input raw data exceeds safety buffer limits.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 3. Normalization
        String normalized = value.strip().replaceAll("\\s{2,}", " ");

        // 4. Size & Boundary
        // Logic: Uses your specific MIN (1) and MAX (20) constants
        DomainGuard.lengthBetween(normalized, MIN_LENGTH, MAX_LENGTH, "Label Value");

        // 5. Lexical Content
        DomainGuard.matches(normalized, ALLOWED_CHARS_PATTERN, "Label Value");

        // 6. Canonical Assignment
        value = normalized;
    }

}
