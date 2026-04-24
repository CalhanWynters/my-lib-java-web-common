package com.github.calhanwynters.domain.validationchecks;

import com.github.calhanwynters.domain.exceptions.DomainRuleViolationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * DomainGuard: Centralized business invariant enforcement.
 * Optimized for Java 21+ (2026 Edition).
 */
@SuppressWarnings("unused")
public final class DomainGuard {

    private DomainGuard() {}

    private static void throwDomainRuleViolation(String message, String errorCode, String violatedRule) {
        // Ensure the parameter name and the usage here match your Exception constructor
        throw new DomainRuleViolationException(message, errorCode, violatedRule);
    }


    // 1. EXISTENCE
    public static <T> T notNull(T value, String fieldName) {
        if (value == null) {
            throwDomainRuleViolation(fieldName + " is required.", "VAL-001", "EXISTENCE");
        }
        return value;
    }

    // 2. TEXTUAL CONTENT (Blank check + Strip)
    public static String notBlank(String value, String fieldName) {
        notNull(value, fieldName);  // Expecting this to throw if value is null
        if (value.isBlank()) {
            throwDomainRuleViolation(fieldName + " is blank.", "VAL-010", "TEXT_CONTENT");
        }
        return value.strip();
    }

    // 3. SIZE (Strings - Updated to use notBlank logic internally)
    public static String lengthBetween(String value, int min, int max, String fieldName) {
        String stripped = notBlank(value, fieldName);
        int length = stripped.length();
        if (length < min || length > max) {
            throwDomainRuleViolation(
                    "%s size must be between %d and %d (received: %d).".formatted(fieldName, min, max, length),
                    "VAL-002", "SIZE"
            );
        }
        return stripped;
    }

    // 4. SYNTAX (Regex)
    public static String matches(String value, Pattern pattern, String fieldName) {
        notBlank(value, fieldName);
        if (!pattern.matcher(value).matches()) {
            throwDomainRuleViolation(
                    "%s format is invalid (received: '%s').".formatted(fieldName, value),
                    "VAL-004", "SYNTAX"
            );
        }
        return value;
    }

    // 5. SEMANTICS (Financial & IDs)
    public static BigDecimal nonNegative(BigDecimal value, String fieldName) {
        notNull(value, fieldName);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throwDomainRuleViolation(
                    "%s must be non-negative (received: %s).".formatted(fieldName, value.toPlainString()),
                    "VAL-005", "SEMANTICS"
            );
        }
        return value;
    }

    public static BigDecimal positive(BigDecimal value, String fieldName) {
        notNull(value, fieldName);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throwDomainRuleViolation(
                    "%s must be strictly positive (received: %s).".formatted(fieldName, value.toPlainString()),
                    "VAL-011", "SEMANTICS"
            );
        }
        return value;
    }

    public static long positive(long value, String fieldName) {
        if (value <= 0) {
            throwDomainRuleViolation(
                    "%s must be a positive number (received: %d).".formatted(fieldName, value),
                    "VAL-013", "RANGE"
            );
        }
        return value;
    }

    public static void positive(int value, String fieldName) {
        if (value <= 0) {
            throwDomainRuleViolation(
                    "%s must be a positive number (received: %d).".formatted(fieldName, value),
                    "VAL-013", "RANGE"
            );
        }
    }


    // 6. COLLECTIONS
    public static <T extends java.util.SequencedCollection<?>> T firstNotNull(T collection, String fieldName) {
        notEmpty(collection, fieldName); // Now calls the local method above
        if (collection.getFirst() == null) {
            throwDomainRuleViolation(
                    "%s must have a non-null primary (first) element.".formatted(fieldName),
                    "VAL-015", "COLLECTION_ORDER"
            );
        }
        return collection;
    }

    public static <T extends Collection<?>> T notEmpty(T collection, String fieldName) {
        notNull(collection, fieldName);
        if (collection.isEmpty()) {
            throwDomainRuleViolation(
                    "%s must contain at least one element.".formatted(fieldName),
                    "VAL-003", "COLLECTION_MIN_SIZE"
            );
        }
        return collection;
    }

    public static <T extends Collection<?>> T noNullElements(T collection, String fieldName) {
        notEmpty(collection, fieldName); // Now calls the local method above
        if (collection.stream().anyMatch(Objects::isNull)) {
            throwDomainRuleViolation(
                    "%s contains null elements.".formatted(fieldName),
                    "VAL-012", "COLLECTION_INTEGRITY"
            );
        }
        return collection;
    }

    // 7. RANGE (Numeric)
    public static int range(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throwDomainRuleViolation(
                    "%s must be between %d and %d (received: %d).".formatted(fieldName, min, max, value),
                    "VAL-007", "RANGE"
            );
        }
        return value;
    }

    // 8. TEMPORAL (Updated for 2026 with Clock Drift Tolerance)
    private static final Duration CLOCK_DRIFT_TOLERANCE = Duration.ofMillis(500);

    /**
     * Validates that an instant is in the past, allowing for minor clock drift.
     * @param clock The clock to use for "now" (useful for testing or shared system time).
     */
    // 8. TEMPORAL (Updated for 2026 with Clock Drift Tolerance)
    public static Instant inPast(Instant date, String fieldName, Clock clock) {
        notNull(date, fieldName);
        // Use .plus(CLOCK_DRIFT_TOLERANCE) to allow minor skew
        if (date.isAfter(clock.instant().plus(CLOCK_DRIFT_TOLERANCE))) {
            throwDomainRuleViolation(
                    // CHANGE: Match the test's expected "cannot be" phrasing
                    "%s cannot be in the future (received: %s).".formatted(fieldName, date),
                    "VAL-008", "TEMPORAL"
            );
        }
        return date;
    }

    /**
     * Validates that an instant is in the future, allowing for minor clock drift.
     */
    public static Instant inFuture(Instant date, String fieldName, Clock clock) {
        notNull(date, fieldName);
        // Tolerance allows "now" to be slightly behind the provided date due to drift
        if (date.isBefore(clock.instant().minus(CLOCK_DRIFT_TOLERANCE))) {
            throwDomainRuleViolation(
                    "%s cannot be in the past (received: %s).".formatted(fieldName, date),
                    "VAL-009", "TEMPORAL"
            );
        }
        return date;
    }

    // Overloads for standard system clock usage
    public static Instant inPast(Instant date, String fieldName) {
        return inPast(date, fieldName, Clock.systemUTC());
    }

    public static Instant inFuture(Instant date, String fieldName) {
        return inFuture(date, fieldName, Clock.systemUTC());
    }


    // 9. GENERIC
    public static void ensure(boolean condition, String message, String errorCode, String ruleName) {
        if (!condition) {
            throwDomainRuleViolation(message, errorCode, ruleName);
        }
    }
}
