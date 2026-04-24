package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.util.regex.Pattern;

/**
 * Hardened Region Value Object.
 */
public record Region(String value) {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;
    private static final int SAFETY_BUFFER = MAX_LENGTH * 2;

    private static final Pattern ALLOWED_CHARS_PATTERN =
            Pattern.compile("^[a-zA-Z0-9](?:[a-zA-Z0-9 -]*[a-zA-Z0-9])?$");

    public static Region from(String value) {
        return new Region(value);
    }

    public static final Region GLOBAL = Region.from("GLOBAL");

    public Region {
        // 1. Existence Check
        DomainGuard.notBlank(value, "Region Value");

        // 2. DoS Mitigation
        DomainGuard.ensure(
                value.length() <= SAFETY_BUFFER,
                "Input raw data exceeds safety buffer limits.",
                "VAL-014",
                "DOS_PREVENTION"
        );

        // 3. Normalization
        String normalized = value.strip().replaceAll("\\s{2,}", " ");

        // 4. Size Boundary
        normalized = DomainGuard.lengthBetween(normalized, MIN_LENGTH, MAX_LENGTH, "Region Value");

        // 5. Lexical Content
        DomainGuard.matches(normalized, ALLOWED_CHARS_PATTERN, "Region Value");

        // 6. Assignment
        value = normalized;
    }
}
