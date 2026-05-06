package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

// FIX: Added the 'public' modifier
public record Money(BigDecimal amount, Currency currency, int precision, RoundingMode roundingMode)
        implements Comparable<Money> {

    public Money {
        DomainGuard.notNull(amount, "Amount");
        DomainGuard.notNull(currency, "Currency");
        DomainGuard.notNull(roundingMode, "Rounding Mode");
        DomainGuard.nonNegative(BigDecimal.valueOf(precision), "Precision");
        // Ensure the amount is scaled immediately upon construction for consistency
        amount = amount.setScale(precision, roundingMode);
    }

    public Money(BigDecimal amount, Currency currency) {
        this(amount, currency, 2, RoundingMode.HALF_UP);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public Money add(Money other) {
        DomainGuard.ensure(
                this.currency.equals(other.currency) && this.precision == other.precision,
                "Cannot add money objects with different currencies or precision settings.",
                "VAL-MONEY-001",
                "CURRENCY_MISMATCH"
        );
        BigDecimal result = this.amount.add(other.amount);
        return new Money(result, this.currency, this.precision, this.roundingMode);
    }

    public Money subtract(Money other) {
        DomainGuard.ensure(
                this.currency.equals(other.currency) && this.precision == other.precision,
                "Cannot subtract money objects with different currencies or precision settings.",
                "VAL-MONEY-002",
                "CURRENCY_MISMATCH"
        );
        BigDecimal result = this.amount.subtract(other.amount);
        return new Money(result, this.currency, this.precision, this.roundingMode);
    }

    public Money negate() {
        BigDecimal result = this.amount.negate();
        return new Money(result, this.currency, this.precision, this.roundingMode);
    }

    public Money multiply(int multiplier) {
        // --- REVISION 1: Changed "positive" to "nonNegative" to allow multiplying by zero ---
        DomainGuard.nonNegative(BigDecimal.valueOf(multiplier), "Int-based Multiplier must be non-negative.");
        BigDecimal result = this.amount.multiply(BigDecimal.valueOf(multiplier));
        return new Money(result, this.currency, this.precision, this.roundingMode);
    }

    /**
     * Overloaded method to multiply the money amount by a BigDecimal factor (fractional quantities).
     * The result is scaled and rounded according to the Money object's precision rules.
     * Decided to keep int based multiplying to acts as a safeguard against corrupted input data.
     */
    public Money multiply(BigDecimal factor) {
        DomainGuard.nonNegative(factor, "Fraction-based Multiplying factor must be non-negative.");
        // The result is automatically scaled/rounded in the compact constructor
        BigDecimal result = this.amount.multiply(factor);
        return new Money(result, this.currency, this.precision, this.roundingMode);
    }

    public Money divide(int divisor) {
        DomainGuard.positive(divisor, "Divisor must be positive.");

        // --- REVISION 2: Use BigDecimal.valueOf for cleaner code ---
        BigDecimal result = this.amount.divide(BigDecimal.valueOf(divisor), this.precision, this.roundingMode);

        return new Money(result, this.currency, this.precision, this.roundingMode);
    }

    @Override
    public int compareTo(Money other) {
        DomainGuard.ensure(
                this.currency.equals(other.currency),
                "Cannot compare different currencies.",
                "VAL-MONEY-003",
                "CURRENCY_MISMATCH"
        );
        return this.amount.compareTo(other.amount);
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public boolean equalsValue(Money other) {
        if (other == null) return false;
        // Compare based on the immutable scaled amount
        boolean amountsEqual = this.amount.compareTo(other.amount) == 0;
        boolean currenciesEqual = this.currency.equals(other.currency);
        return amountsEqual && currenciesEqual;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return String.format("%s %s", currency.getCurrencyCode(), amount.toPlainString());
    }
}
