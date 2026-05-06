package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;

/**
 * Value Object for Primary Keys.
 * Hardened for Java 21/25 (2026 Edition) using DomainGuard.
 */
public record PkId(Long value) {

    // Boundary: Maximum logical value for a Long PK to prevent overflow exploitation
    private static final long MAX_PK_VALUE = Long.MAX_VALUE - 1000;

    /**
     * Compact Constructor enforcing positive range and safety boundaries.
     */
    public PkId {
        // 1. Existence
        DomainGuard.notNull(value, "Primary Key");

        // 2. Positivity (Handles Long types via the generic switch)
        DomainGuard.positiveGeneric(value, "Primary Key");

        // 3. Safety Boundary
        DomainGuard.range(value, null, MAX_PK_VALUE, "Primary Key");
    }


    /**
     * Factory method for creating an ID from a raw long.
     */
    public static PkId of(long value) {
        return new PkId(value);
    }

    /**
     * Overloaded factory method to satisfy test architecture.
     */
    public static PkId fromString(long value) {
        return new PkId(value);
    }

    /**
     * Helper for string-based inputs (API Gateways/Web layers).
     */
    public static PkId fromString(String rawValue) {
        DomainGuard.notBlank(rawValue, "Input ID String");
        try {
            return new PkId(Long.parseLong(rawValue.strip()));
        } catch (NumberFormatException e) {
            // Re-wrapping in a DomainRuleViolationException via ensure logic
            DomainGuard.ensure(false, "Invalid ID format: must be a valid numeric long.", "VAL-004", "SYNTAX");
            return null; // Unreachable
        }
    }
}
