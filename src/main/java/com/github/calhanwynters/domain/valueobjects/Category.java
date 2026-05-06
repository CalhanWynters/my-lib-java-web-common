package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.AllowedList;
import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.util.Set;
import java.util.regex.Pattern;

public record Category(String value) {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;
    private static final int SAFETY_BUFFER = MAX_LENGTH * 2;

    // Fix 1: Use AllowedList record instead of raw Set
    private static final AllowedList ALLOWED_TAXONOMY = new AllowedList(Set.of(
            "Electronics", "Home", "Garden", "Fashion", "Beauty"
    ));

    // Syntax: Unicode-aware pattern (Letters, Numbers, Spaces)
    private static final Pattern VALID_CHARS = Pattern.compile("^[\\p{L}\\p{N} ]+$");

    public Category {
        // 1. EXISTENCE
        DomainGuard.notBlank(value, "Category");

        // 2. DOS MITIGATION
        DomainGuard.ensure(
                value.length() <= SAFETY_BUFFER,
                "Input raw data exceeds safety buffer limits.",
                "VAL-014", "DOS_PREVENTION"
        );

        // Normalization
        String normalized = value.strip().replaceAll("\\s{2,}", " ");

        // 3. SIZE
        // Note: Ensure your DomainGuard has an overload for (value, min, max, fieldName)
        // or this will use the global Config defaults.
        DomainGuard.lengthBetween(normalized, "Category");

        // 4. LEXICAL: Integrity & Whitelisting
        // Fix 2: Remove the predicate; logic now resides in DomainGuard/Config
        DomainGuard.noProfanity(normalized, "Category");

        // Fix 3: Pass the AllowedList object
        DomainGuard.inAllowedList(normalized, ALLOWED_TAXONOMY, "Category");

        // 5. SYNTAX
        DomainGuard.matches(normalized, VALID_CHARS, "Category");

        // 6. INITIALIZATION
        value = normalized;
    }
}
