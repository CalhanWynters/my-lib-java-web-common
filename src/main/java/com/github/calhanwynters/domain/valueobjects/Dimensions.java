package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Hardened Dimensions Value Object for 2026 Edition.
 * Enforces strict non-scientific notation and logical safety boundaries via DomainGuard.
 */
public record Dimensions(BigDecimal length, BigDecimal width, BigDecimal height, DimensionUnitEnums sizeUnit) {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,10})?$");
    private static final BigDecimal ABSOLUTE_MAX_LIMIT = new BigDecimal("10000.0");
    private static final int MAX_INPUT_STR_LENGTH = 16;

    // Define the Null Object with values that pass the guards
    // or signify a "Neutral" state.
    public static final Dimensions NONE = new Dimensions(
            new BigDecimal("0.0000000001"), // Smallest positive value to pass DomainGuard.positive
            new BigDecimal("0.0000000001"),
            new BigDecimal("0.0000000001"),
            DimensionUnitEnums.NONE
    );

    // Helper to check for the Null Object state
    public boolean isNone() {
        return this.equals(NONE) || this.sizeUnit == DimensionUnitEnums.NONE;
    }

    /**
     * Factory method: Strictly enforces non-scientific notation from String inputs.
     * Prevents precision-loss attacks common in 2026 API integrations.
     */
    public static Dimensions of(String lengthStr, String widthStr, String heightStr, DimensionUnitEnums unit) {
        return new Dimensions(
                parseStrict(lengthStr, "Length"),
                parseStrict(widthStr, "Width"),
                parseStrict(heightStr, "Height"),
                unit
        );
    }

    private static BigDecimal parseStrict(String input, String fieldName) {
        // 1. Existence and Security Length Check (Throws VAL-010 / VAL-014)
        DomainGuard.notBlank(input, fieldName);
        DomainGuard.ensure(
                input.length() <= MAX_INPUT_STR_LENGTH,
                fieldName + " input exceeds security length boundary.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 2. Lexical Format Check (Throws VAL-004)
        DomainGuard.matches(input, NUMERIC_PATTERN, fieldName);

        return new BigDecimal(input);
    }

    /**
     * Compact Constructor enforcing "Always-Valid" dimension invariants.
     */
    public Dimensions {
        // 1. Existence (Throws VAL-001)
        DomainGuard.notNull(length, "Length");
        DomainGuard.notNull(width, "Width");
        DomainGuard.notNull(height, "Height");
        DomainGuard.notNull(sizeUnit, "Dimension Unit");

        // 2. Logical Invariants (Throws VAL-011 / VAL-007)
        validateLogical(length, "Length");
        validateLogical(width, "Width");
        validateLogical(height, "Height");
    }

    private void validateLogical(BigDecimal value, String name) {
        // Enforce strictly positive (VAL-011)
        DomainGuard.positive(value, name);

        // Enforce upper safety limit (VAL-007)
        DomainGuard.ensure(
                value.compareTo(ABSOLUTE_MAX_LIMIT) <= 0,
                "%s must be below %s".formatted(name, ABSOLUTE_MAX_LIMIT.toPlainString()),
                "VAL-007", "RANGE"
        );
    }
}
