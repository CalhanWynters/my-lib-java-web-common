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

        // Use positiveGeneric for the 'int' precision
        DomainGuard.positiveGeneric(precision, "Precision");

        // Scale immediately to ensure the Record's state is canonical
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
        // ✅ Fix: Use the new generic guard to allow 0 without manual +1 hacks
        DomainGuard.nonNegativeGeneric(multiplier, "Multiplier");

        BigDecimal result = this.amount.multiply(BigDecimal.valueOf(multiplier));
        return new Money(result, this.currency, this.precision, this.roundingMode);
    }

    public Money multiply(BigDecimal factor) {
        // ✅ Fix: Standardized to use the same generic logic for consistency
        DomainGuard.nonNegativeGeneric(factor, "Multiplying factor");

        BigDecimal result = this.amount.multiply(factor);
        return new Money(result, this.currency, this.precision, this.roundingMode);
    }

    public Money divide(int divisor) {
        // Use positiveGeneric to ensure divisor is > 0
        DomainGuard.positiveGeneric(divisor, "Divisor");
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
