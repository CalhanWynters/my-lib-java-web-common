package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.util.regex.Pattern;

/**
 * Domain value object for product descriptions.
 * Optimized for Java 21+ (2026 Architecture).
 */
public record Description(String text) {

    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 2000;
    private static final int DOS_SAFETY_BUFFER = MAX_LENGTH * 2;

    // Syntax: Structural Whitelist
    private static final Pattern ALLOWED_CHARS_PATTERN =
            Pattern.compile("^[a-zA-Z0-9 .,:;!\\-?\\n*•()\\[\\]]+$");

    public Description {
        // 1. EXISTENCE
        DomainGuard.notBlank(text, "Description");

        // 2. DOS MITIGATION & NORMALIZATION
        DomainGuard.ensure(text.length() <= DOS_SAFETY_BUFFER,
                "Input raw data exceeds safety buffer limits.", "VAL-014", "DOS_PREVENTION");

        String normalized = text.strip()
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("(?m)^ +| +$", "");

        // 3. SIZE
        // Note: Using explicit bounds check since DomainGuard uses static config
        if (normalized.length() < MIN_LENGTH || normalized.length() > MAX_LENGTH) {
            throw new com.github.calhanwynters.domain.exceptions.DomainRuleViolationException(
                    "Description must be between %d and %d characters.".formatted(MIN_LENGTH, MAX_LENGTH),
                    "VAL-002", "SIZE");
        }

        // 4. LEXICAL: Content Moderation
        // Now standardized to use the DomainGuard's internal config list
        DomainGuard.noProfanity(normalized, "Description");

        // 5. SYNTAX: Pattern Matching
        DomainGuard.matches(normalized, ALLOWED_CHARS_PATTERN, "Description");

        // 6. CANONICAL ASSIGNMENT
        text = normalized;
    }

    public static Description from(String text) {
        return new Description(text);
    }

    public Description truncate(int maxLength) {
        // Use positiveGeneric for the 'int' parameter
        DomainGuard.positiveGeneric(maxLength, "Truncation Length");

        if (text.length() <= maxLength) {
            return this;
        }

        String truncated = text.substring(0, Math.max(0, maxLength - 3)) + "...";
        return new Description(truncated);
    }
}
