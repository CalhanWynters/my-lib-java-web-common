package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.AllowedList;
import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Hardened Domain Status Enum for Java 21/25 (2026 Edition).
 * Aligned with DomainGuard for standardized error handling.
 */
public enum StatusEnums {
    ACTIVE,
    DRAFT,
    INACTIVE,
    DISCONTINUED;

    /**
     * Fix: Wrap the Set in an AllowedList to match DomainGuard's lexical signature.
     */
    private static final AllowedList ALLOWED_STATUSES = new AllowedList(
            Arrays.stream(values())
                    .map(Enum::name)
                    .collect(Collectors.toSet())
    );

    /**
     * Safe parser for external inputs using DomainGuard.
     */
    public static StatusEnums fromString(String value) {
        // 1. Existence and initial content (Throws VAL-010)
        DomainGuard.notBlank(value, "Status");

        // 2. Normalization & DoS Mitigation
        String normalized = value.strip().toUpperCase();
        DomainGuard.ensure(
                normalized.length() <= 20,
                "Status input exceeds logical boundary.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 3. Lexical Validation (Throws VAL-020: LEXICAL_CONTENT)
        // Standardizing to use the library's built-in whitelist check
        DomainGuard.inAllowedList(normalized, ALLOWED_STATUSES, "Status");

        return StatusEnums.valueOf(normalized);
    }

    /**
     * Semantic state transition logic with Domain Guarding.
     */
    public boolean canTransitionTo(StatusEnums nextStatus) {
        // 1. Existence Check
        DomainGuard.notNull(nextStatus, "Target Status");

        // 2. Business Rule: Logical State Machine
        return switch (this) {
            case DRAFT -> true; // Drafts can move anywhere
            case ACTIVE -> nextStatus != DRAFT; // Active items can't go back to Draft
            case INACTIVE, DISCONTINUED -> nextStatus == ACTIVE; // Must go back to Active first
        };
    }
}
