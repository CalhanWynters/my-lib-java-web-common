package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.AllowedList;
import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Dimension Units for physical product measurements.
 * Aligned with DomainGuard for standardized Lexical reporting.
 */
public enum DimensionUnitEnums {
    CM("CM"),
    MM("MM"),
    IN("IN"),
    FT("FT"),
    NONE("NONE");

    private final String code;

    /**
     * Fix: Wrap the Set in an AllowedList to match DomainGuard signature.
     * Pre-computed for high-performance lookup.
     */
    private static final AllowedList ALLOWED_CODES = new AllowedList(
            Arrays.stream(values())
                    .map(DimensionUnitEnums::getCode)
                    .collect(Collectors.toSet())
    );

    DimensionUnitEnums(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Safe parser using Lexical Whitelisting via DomainGuard.
     */
    public static DimensionUnitEnums fromCode(String code) {
        // 1. EXISTENCE
        DomainGuard.notBlank(code, "Dimension Unit Code");

        // 2. NORMALIZATION
        String normalizedCode = code.strip().toUpperCase();

        // 3. LEXICAL: Whitelist Validation (Throws VAL-020 if invalid)
        DomainGuard.inAllowedList(normalizedCode, ALLOWED_CODES, "Dimension Unit Code");

        // 4. CANONICAL LOOKUP
        return DimensionUnitEnums.valueOf(normalizedCode);
    }
}
