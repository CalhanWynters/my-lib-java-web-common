package com.github.calhanwynters.domain.common.valueobjects;

import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Domain value object for product descriptions.
 * Optimized for Java 21+ (2026 Architecture).
 */
public record Description(String text) {

    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 2000;
    private static final int DOS_SAFETY_BUFFER = MAX_LENGTH * 2;

    private static final Pattern ALLOWED_CHARS_PATTERN =
            Pattern.compile("^[a-zA-Z0-9 .,:;!\\-?\\n*•()\\[\\]]+$");

    private static final Set<String> FORBIDDEN_WORDS = Set.of("forbiddenword1", "forbiddenword2");

    public static Description from(String text) {
        return new Description(text);
    }

    public Description {
        // 1. Existence & Initial Content
        DomainGuard.notBlank(text, "Description");

        // 2. Pre-normalization Security Buffer (DoS Mitigation)
        DomainGuard.ensure(text.length() <= DOS_SAFETY_BUFFER,
                "Input raw data exceeds safety buffer limits.", "VAL-DESC-001", "SECURITY");

        // 3. Normalization
        String normalized = text.strip()
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("(?m)^ +| +$", "");

        // 4. Lexical Content (Injection Prevention)
        DomainGuard.matches(normalized, ALLOWED_CHARS_PATTERN, "Description");

        // 5. Size Validation
        // This utilizes lengthBetween which returns the stripped string and checks MIN/MAX
        normalized = DomainGuard.lengthBetween(normalized, MIN_LENGTH, MAX_LENGTH, "Description");

        // 6. Semantics (Forbidden Words)
        String lowerNormalized = normalized.toLowerCase();
        boolean hasForbidden = FORBIDDEN_WORDS.stream().anyMatch(lowerNormalized::contains);
        DomainGuard.ensure(!hasForbidden,
                "Description violates content security policies.", "VAL-DESC-002", "CONTENT_MODERATION");

        // 7. Canonical Assignment
        text = normalized;
    }

    public Description truncate(int maxLength) {
        if (text.length() <= maxLength) {
            return this;
        }
        // Use DomainGuard to ensure the maxLength passed into the behavior is valid
        DomainGuard.positive(maxLength, "Truncation Length");

        String truncated = text.substring(0, Math.max(0, maxLength - 3)) + "...";
        return new Description(truncated);
    }
}
