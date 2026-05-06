package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Hardened Category Value Object for Java 21/25 (2026 Edition).
 * Hierarchical validation: Existence -> Size -> Lexical -> Syntax.
 */
public record Category(String value) {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;
    private static final int SAFETY_BUFFER = MAX_LENGTH * 2;

    // Lexical: Official Business Taxonomy
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "Electronics", "Home", "Garden", "Fashion", "Beauty"
    );

    // Lexical: Content Integrity
    private static final Predicate<String> PROFANITY_FILTER =
            s -> s.toLowerCase().contains("badword");

    // Syntax: Unicode-aware pattern (Letters, Numbers, Spaces)
    private static final Pattern VALID_CHARS = Pattern.compile("^[\\p{L}\\p{N} ]+$");

    /**
     * Compact Constructor enforcing hierarchical domain guards.
     */
    public Category {
        // 1. EXISTENCE
        DomainGuard.notBlank(value, "Category");

        // 2. DOS MITIGATION & SIZE
        DomainGuard.ensure(
                value.length() <= SAFETY_BUFFER,
                "Input raw data exceeds safety buffer limits.",
                "VAL-014", "DOS_PREVENTION"
        );

        // Normalization (Prep for domain logic)
        String normalized = value.strip().replaceAll("\\s{2,}", " ");
        DomainGuard.lengthBetween(normalized, MIN_LENGTH, MAX_LENGTH, "Category");

        // 3. LEXICAL: Integrity & Whitelisting
        DomainGuard.noProfanity(normalized, PROFANITY_FILTER, "Category");
        DomainGuard.inAllowedList(normalized, ALLOWED_CATEGORIES, "Category");

        // 4. SYNTAX: Pattern Matching
        DomainGuard.matches(normalized, VALID_CHARS, "Category");

        // 5. INITIALIZATION
        value = normalized;
    }
}
