package com.github.calhanwynters.domain.common.valueobjects;

import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

import java.util.Arrays;
import java.util.Set;
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

    private static final Set<String> VALID_NAMES = Arrays.stream(StatusEnums.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    /**
     * Safe parser for external inputs using DomainGuard.
     */
    public static StatusEnums fromString(String value) {
        // 1. Existence and initial content (Throws VAL-010)
        DomainGuard.notBlank(value, "Status");

        // 2. Normalization & DoS Mitigation (Throws VAL-014)
        String normalized = value.strip().toUpperCase();
        DomainGuard.ensure(
                normalized.length() <= 20,
                "Status input exceeds logical boundary.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 3. Whitelist Validation (Throws VAL-004)
        DomainGuard.ensure(
                VALID_NAMES.contains(normalized),
                "Invalid Status: '%s'. Allowed values: %s".formatted(normalized, VALID_NAMES),
                "VAL-004", "SYNTAX"
        );

        return StatusEnums.valueOf(normalized);
    }

    /**
     * Semantic state transition logic.
     */
    public boolean canTransitionTo(StatusEnums nextStatus) {
        // Throws VAL-001
        DomainGuard.notNull(nextStatus, "Target Status");

        return switch (this) {
            case DRAFT -> true;
            case ACTIVE -> nextStatus != DRAFT;
            case INACTIVE, DISCONTINUED -> nextStatus == ACTIVE;
        };
    }
}
