package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.AllowedList;
import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Standardized Weight Units for product variants.
 * Aligned with DomainGuard for the 2026 Edition.
 */
public enum WeightUnitEnums {
    GRAM,
    KILOGRAM,
    POUND,
    OUNCE,
    CARAT,
    TROY_OUNCE,
    NONE;

    /**
     * Pre-computed AllowedList for high-performance lexical validation.
     */
    private static final AllowedList ALLOWED_UNITS = new AllowedList(
            Arrays.stream(values())
                    .map(Enum::name)
                    .collect(Collectors.toSet())
    );

    /**
     * Safe parser for weight units using DomainGuard.
     * Enforces strict uppercase matching and handles normalization.
     */
    public static WeightUnitEnums fromString(String value) {
        // 1. Existence and Blank check (Throws VAL-010)
        DomainGuard.notBlank(value, "Weight Unit");

        // 2. Normalization
        String normalized = value.strip().toUpperCase();

        // 3. Lexical Whitelist Validation (Throws VAL-020)
        DomainGuard.inAllowedList(normalized, ALLOWED_UNITS, "Weight Unit");

        // 4. Canonical Lookup
        return WeightUnitEnums.valueOf(normalized);
    }
}
