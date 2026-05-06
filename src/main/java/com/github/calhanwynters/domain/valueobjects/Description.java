package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.util.function.Predicate;
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

    // Lexical: Content Integrity
    private static final Predicate<String> PROFANITY_FILTER = text ->
            text.toLowerCase().contains("forbiddenword1") || text.toLowerCase().contains("forbiddenword2");

    /**
     * Compact Constructor enforcing hierarchical domain guards.
     */
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
        DomainGuard.lengthBetween(normalized, MIN_LENGTH, MAX_LENGTH, "Description");

        // 4. LEXICAL: Content Moderation (Standardized)
        DomainGuard.noProfanity(normalized, PROFANITY_FILTER, "Description");

        // 5. SYNTAX: Pattern Matching
        DomainGuard.matches(normalized, ALLOWED_CHARS_PATTERN, "Description");

        // 6. CANONICAL ASSIGNMENT
        text = normalized;
    }

    public static Description from(String text) {
        return new Description(text);
    }

    public Description truncate(int maxLength) {
        if (text.length() <= maxLength) {
            return this;
        }
        DomainGuard.positive(maxLength, "Truncation Length");

        String truncated = text.substring(0, Math.max(0, maxLength - 3)) + "...";
        return new Description(truncated);
    }
}
