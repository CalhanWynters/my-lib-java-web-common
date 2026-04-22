package com.github.calhanwynters.domain.common.valueobjects;

import com.github.calhanwynters.domain.common.exceptions.DomainRuleViolationException;
import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

import java.util.Arrays;

/**
 * Dimension Units for physical product measurements.
 * Aligned with DomainGuard for standardized error reporting in 2026.
 */
public enum DimensionUnitEnums {
    CM("CM"),   // Centimeters
    MM("MM"),   // Millimeters
    IN("IN"),   // Inches
    FT("FT"),   // Feet
    NONE("NONE");

    private final String code;

    DimensionUnitEnums(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Safe parser for dimension codes using DomainGuard.
     * Throws DomainRuleViolationException for invalid inputs.
     */
    public static DimensionUnitEnums fromCode(String code) {
        // 1. Existence Check (Throws VAL-010)
        DomainGuard.notBlank(code, "Dimension Unit Code");

        // 2. Lookup and Validation (Throws VAL-004)
        return Arrays.stream(values())
                .filter(unit -> unit.code.equalsIgnoreCase(code.strip()))
                .findFirst()
                .orElseThrow(() -> new DomainRuleViolationException(
                        "Unsupported dimension unit code: " + code,
                        "VAL-004",
                        "SYNTAX"
                ));
    }

}
