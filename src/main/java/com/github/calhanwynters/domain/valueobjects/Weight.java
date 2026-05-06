package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Hardened Weight Value Object for 2026 Edition.
 * Enforces numeric precision and logical safety boundaries via DomainGuard.
 */
public record Weight(BigDecimal amount, WeightUnitEnums weightUnit) {

    public static final Weight NONE = new Weight(BigDecimal.ZERO, WeightUnitEnums.NONE);

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,5})?$");
    private static final int MAX_SERIALIZED_LENGTH = 32;

    public boolean isNone() {
        return this.equals(NONE) || this.weightUnit == WeightUnitEnums.NONE;
    }

    /**
     * Compact Constructor enforcing "Always-Valid" weight invariants.
     */
    public Weight {
        // 1. EXISTENCE
        DomainGuard.notNull(amount, "Weight Amount");
        DomainGuard.notNull(weightUnit, "Weight Unit");

        // 2. DOS MITIGATION
        String plainAmount = amount.toPlainString();
        DomainGuard.ensure(
                plainAmount.length() <= MAX_SERIALIZED_LENGTH,
                "Weight input exceeds security boundary.",
                "VAL-014", "DOS_PREVENTION"
        );

        // 3. SYNTAX: Numeric Pattern (Prevents scientific notation/malformed input)
        DomainGuard.matches(plainAmount, NUMERIC_PATTERN, "Weight Amount");

        // 4. SEMANTICS: Precision & Signum
        // Note: Ensure your global DomainGuardConfig is set to 5 for Weight contexts
        DomainGuard.maxScale(amount, "Weight Amount");

        // Correctly allows 0.0 for the NONE constant
        DomainGuard.nonNegativeGeneric(amount, "Weight Amount");
    }
}
