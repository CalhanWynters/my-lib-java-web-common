package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Hardened Dimensions Value Object for 2026 Edition.
 * Enforces strict non-scientific notation and logical safety boundaries.
 */
public record Dimensions(BigDecimal length, BigDecimal width, BigDecimal height, DimensionUnitEnums sizeUnit) {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,10})?$");
    private static final BigDecimal ABSOLUTE_MAX_LIMIT = new BigDecimal("10000.0");
    private static final int MAX_INPUT_STR_LENGTH = 16;

    public static final Dimensions NONE = new Dimensions(
            new BigDecimal("0.0000000001"),
            new BigDecimal("0.0000000001"),
            new BigDecimal("0.0000000001"),
            DimensionUnitEnums.NONE
    );

    public boolean isNone() {
        return this.equals(NONE) || this.sizeUnit == DimensionUnitEnums.NONE;
    }

    public static Dimensions of(String lengthStr, String widthStr, String heightStr, DimensionUnitEnums unit) {
        return new Dimensions(
                parseStrict(lengthStr, "Length"),
                parseStrict(widthStr, "Width"),
                parseStrict(heightStr, "Height"),
                unit
        );
    }

    private static BigDecimal parseStrict(String input, String fieldName) {
        // 1. EXISTENCE
        DomainGuard.notBlank(input, fieldName);

        // 2. DOS MITIGATION
        DomainGuard.ensure(
                input.length() <= MAX_INPUT_STR_LENGTH,
                fieldName + " input exceeds security length boundary.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 3. SYNTAX: Numeric Pattern
        DomainGuard.matches(input, NUMERIC_PATTERN, fieldName);

        return new BigDecimal(input);
    }

    /**
     * Compact Constructor enforcing "Always-Valid" dimension invariants.
     */
    public Dimensions {
        // 1. EXISTENCE
        DomainGuard.notNull(length, "Length");
        DomainGuard.notNull(width, "Width");
        DomainGuard.notNull(height, "Height");
        DomainGuard.notNull(sizeUnit, "Dimension Unit");

        // 2. SEMANTICS: Positive Range (VAL-011)
        DomainGuard.positive(length, "Length");
        DomainGuard.positive(width, "Width");
        DomainGuard.positive(height, "Height");

        // 3. SEMANTICS: Upper Boundary Range (VAL-007)
        validateRange(length, "Length");
        validateRange(width, "Width");
        validateRange(height, "Height");
    }

    private void validateRange(BigDecimal value, String name) {
        DomainGuard.ensure(
                value.compareTo(ABSOLUTE_MAX_LIMIT) <= 0,
                "%s must be below %s".formatted(name, ABSOLUTE_MAX_LIMIT.toPlainString()),
                "VAL-007", "RANGE"
        );
    }
}
