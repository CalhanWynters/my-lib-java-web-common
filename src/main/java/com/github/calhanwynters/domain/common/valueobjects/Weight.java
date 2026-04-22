package com.github.calhanwynters.domain.common.valueobjects;

import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Hardened Weight Value Object for 2026 Edition.
 * Enforces numeric precision and logical safety boundaries via DomainGuard.
 */
public record Weight(BigDecimal amount, WeightUnitEnums weightUnit) {

    // Concrete Null Object instance
    public static final Weight NONE = new Weight(BigDecimal.ZERO, WeightUnitEnums.NONE);

    public boolean isNone() {
        return this.equals(NONE) || this.weightUnit == WeightUnitEnums.NONE;
    }

    // Lexical Content: Whitelist for numeric inputs (up to 5 decimal places)
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,5})?$");

    // Size & Boundary: Max string length for BigDecimal to prevent parsing attacks
    private static final int MAX_SERIALIZED_LENGTH = 32;

    // Semantics: Max input scale for precision
    private static final int MAX_INPUT_SCALE = 5;

    /**
     * Compact Constructor enforcing "Always-Valid" weight invariants.
     */
    public Weight {
        // 1. Existence (Throws VAL-001)
        DomainGuard.notNull(amount, "Weight Amount");
        DomainGuard.notNull(weightUnit, "Weight Unit");

        // 2. Size & Boundary (DoS Prevention - Throws VAL-014)
        String plainAmount = amount.toPlainString();
        DomainGuard.ensure(
                plainAmount.length() <= MAX_SERIALIZED_LENGTH,
                "Input numeric string length exceeds security boundary.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 3. Lexical Content & Syntax (Throws VAL-004)
        DomainGuard.matches(plainAmount, NUMERIC_PATTERN, "Weight Amount");

        // 4. Semantics (Precision & Sign - Throws VAL-011 / VAL-005)
        DomainGuard.ensure(
                amount.scale() <= MAX_INPUT_SCALE,
                "Numeric precision exceeds allowed scale of %d decimal places.".formatted(MAX_INPUT_SCALE),
                "VAL-011", "SEMANTICS"
        );

        DomainGuard.nonNegative(amount, "Weight Amount");
    }
}
