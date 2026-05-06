package com.github.calhanwynters.domain.validationchecks;

import com.github.calhanwynters.domain.exceptions.DomainRuleViolationException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class DomainGuard {

    private DomainGuard() {}

    private static DomainGuardConfig config = new DomainGuardConfig(); // Default config instance

    private static void fail(String msg, String code, String rule) {
        throw new DomainRuleViolationException(msg, code, rule);
    }

    // --- EXISTENCE & TEXT ---

    public static <T> T notNull(T value, String fieldName) {
        if (value == null) {
            fail(fieldName + " is required.", "VAL-001", "EXISTENCE");
        }
        return value;
    }

    public static String notBlank(String value, String fieldName) {
        String cleaned = notNull(value, fieldName).strip();
        if (cleaned.isEmpty()) {
            fail(fieldName + " cannot be blank.", "VAL-010", "TEXT_CONTENT");
        }
        return cleaned;
    }

    public static String lengthBetween(String value, String fieldName) {
        String cleaned = notBlank(value, fieldName);
        int len = cleaned.length();
        if (len < config.getMinLength() || len > config.getMaxLength()) {
            fail("%s length must be [%d..%d] (was: %d)."
                    .formatted(fieldName, config.getMinLength(), config.getMaxLength(), len), "VAL-002", "SIZE");
        }
        return cleaned;
    }

    public static String lengthBetween(String value, int min, int max, String fieldName) {
        String cleaned = notBlank(value, fieldName);
        int len = cleaned.length();
        if (len < min || len > max) {
            fail("%s length must be [%d..%d] (was: %d)."
                    .formatted(fieldName, min, max, len), "VAL-002", "SIZE");
        }
        return cleaned;
    }


    public static String matches(String value, Pattern pattern, String fieldName) {
        notNull(pattern, "Pattern");
        String cleaned = notBlank(value, fieldName);
        if (!pattern.matcher(cleaned).matches()) {
            fail("%s format is invalid.".formatted(fieldName), "VAL-004", "SYNTAX");
        }
        return cleaned;
    }

    // Add private static final AllowedList allowedDomains = new AllowedList(Set.of("example.com", "test.com")); in each VO for unique Allowed List.
    public static String inAllowedList(String value, AllowedList allowed, String fieldName) {
        String cleaned = notBlank(value, fieldName);
        if (!allowed.contains(cleaned)) {
            fail("%s contains unpermitted value: '%s'.".formatted(fieldName, cleaned), "VAL-020", "LEXICAL_CONTENT");
        }
        return cleaned;
    }

    public static String noProfanity(String value, String fieldName) {
        String cleaned = notBlank(value, fieldName);
        config.getProfanityList().findFirstIn(cleaned).ifPresent(word -> fail("%s contains inappropriate language: '%s'.".formatted(fieldName, word),
                "VAL-021", "PROFANITY"));
        return cleaned;
    }


    // --- NUMERIC ---

    public static <T extends Comparable<T>> T range(T value, T min, T max, String fieldName) {
        notNull(value, fieldName);
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            fail("%s must be between %s and %s."
                    .formatted(fieldName, min, max), "VAL-007", "RANGE");
        }
        return value;
    }

    public static <T extends Number> T nonNegativeGeneric(T value, String fieldName) {
        notNull(value, fieldName);
        boolean isInvalid = switch (value) {
            case Integer i -> i < 0;
            case Long l -> l < 0L;
            case Double d -> Double.isNaN(d) || Double.isInfinite(d) || d < 0.0;
            case Float f -> Float.isNaN(f) || Float.isInfinite(f) || f < 0.0f;
            case BigDecimal b -> b.signum() < 0;
            default -> throw new IllegalArgumentException("Unsupported numeric type: " + value.getClass().getName());
        };

        if (isInvalid) {
            fail("%s must be non-negative (received: %s).".formatted(fieldName, value), "VAL-011", "SEMANTICS");
        }
        return value;
    }

    public static BigDecimal nonNegative(BigDecimal value, String fieldName) {
        return range(value, BigDecimal.ZERO, null, fieldName);
    }

    public static BigDecimal positive(BigDecimal value, String fieldName) {
        return range(value, BigDecimal.ONE, null, fieldName); // using BigDecimal.ONE for clarity
    }

    public static BigDecimal between(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName) {
        notNull(value, fieldName);
        if (min != null) {
            if (value.compareTo(min) < 0) {
                fail("%s must be >= %s (was: %s).".formatted(fieldName, min, value), "VAL-007", "NUMERIC_RANGE");
            }
        }
        if (max != null) {
            if (value.compareTo(max) > 0) {
                fail("%s must be <= %s (was: %s).".formatted(fieldName, max, value), "VAL-007", "NUMERIC_RANGE");
            }
        }
        return value;
    }

    public static BigDecimal maxScale(BigDecimal value, String fieldName) {
        notNull(value, fieldName);
        if (value.scale() > config.getMaxScale()) {
            fail("%s cannot exceed %d decimal places (was: %d).".formatted(fieldName, config.getMaxScale(), value.scale()), "VAL-013", "PRECISION");
        }
        return value;
    }

    public static <T extends Number> T positiveGeneric(T value, String fieldName) {
        notNull(value, fieldName);
        boolean isInvalid = switch (value) {
            case Integer i -> i <= 0;
            case Long l -> l <= 0L;
            case Double d -> Double.isNaN(d) || Double.isInfinite(d) || d <= 0.0;
            case Float f -> Float.isNaN(f) || Float.isInfinite(f) || f <= 0.0f;
            case BigDecimal b -> b.signum() <= 0;
            default -> throw new IllegalArgumentException("Unsupported numeric type: " + value.getClass().getName());
        };

        if (isInvalid) {
            fail("%s must be strictly positive (received: %s).".formatted(fieldName, value), "VAL-011", "SEMANTICS");
        }
        return value;
    }

    // --- COLLECTIONS ---

    public static <T extends Collection<?>> T notEmpty(T collection, String fieldName) {
        notNull(collection, fieldName);
        if (collection.isEmpty()) {
            fail("%s cannot be empty.".formatted(fieldName), "VAL-003", "COLLECTION_MIN_SIZE");
        }
        return collection;
    }

    public static <T, C extends Collection<T>> Collection<T> secureNotEmpty(C collection, String fieldName) {
        notEmpty(collection, fieldName);
        return Collections.unmodifiableCollection(collection);
    }

    public static <T, L extends List<T>> List<T> secureList(L list, String fieldName) {
        notNull(list, fieldName);
        return List.copyOf(list);
    }

    public static <T extends Collection<?>> T sizeBetween(T collection, String fieldName) {
        notNull(collection, fieldName);
        int size = collection.size();
        if (size < config.getMinCollectionSize() || size > config.getMaxCollectionSize()) {
            fail("%s size must be [%d..%d] (was: %d).".formatted(fieldName, config.getMinCollectionSize(), config.getMaxCollectionSize(), size), "VAL-006", "COLLECTION_SIZE");
        }
        return collection;
    }

    public static <T extends Collection<?>> T noNullElements(T collection, String fieldName) {
        notEmpty(collection, fieldName);
        if (collection.stream().anyMatch(Objects::isNull)) {
            fail("%s contains null elements.".formatted(fieldName), "VAL-012", "COLLECTION_INTEGRITY");
        }
        return collection;
    }

    public static <T> T firstNotNull(Collection<T> collection, String fieldName) {
        notEmpty(collection, fieldName);
        T first = collection.iterator().next();
        if (first == null) {
            fail("%s primary element is null.".formatted(fieldName), "VAL-015", "COLLECTION_ORDER");
        }
        return first;
    }

    // --- BUSINESS LOGIC WRAPPERS ---

    public static <T> T check(T value, Predicate<T> predicate, String msg, String code, String rule) {
        notNull(value, "Value under validation");
        if (!predicate.test(value)) {
            fail(msg, code, rule);
        }
        return value;
    }

    // --- TEMPORAL ---

    public static void inclusiveRange(Instant value, Instant start, Instant end, String fieldName) {
        notNull(value, fieldName);
        if (value.isBefore(start) || value.isAfter(end)) {
            fail("%s must be between %s and %s.".formatted(fieldName, start, end), "VAL-016", "TEMPORAL_RANGE");
        }
    }

    public static Instant inPast(Instant date, String fieldName, Clock clock) {
        if (notNull(date, fieldName).isAfter(clock.instant().plus(config.getTemporalTolerance()))) {
            fail("%s must be in the past.".formatted(fieldName), "VAL-008", "TEMPORAL");
        }
        return date;
    }

    public static Instant inPast(Instant date, String fieldName) {
        return inPast(date, fieldName, Clock.systemUTC());
    }

    public static Instant inFuture(Instant date, String fieldName, Clock clock) {
        notNull(date, fieldName);
        if (date.isBefore(clock.instant().minus(config.getTemporalTolerance()))) {
            fail("%s must be in the future.".formatted(fieldName), "VAL-009", "TEMPORAL");
        }
        return date;
    }

    public static Instant inFuture(Instant date, String fieldName) {
        return inFuture(date, fieldName, Clock.systemUTC());
    }

    public static Instant between(Instant date, Instant minDate, Instant maxDate, String fieldName) {
        notNull(date, fieldName);
        if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            fail("%s must be between %s and %s (was: %s).".formatted(fieldName, minDate, maxDate, date), "VAL-016", "TEMPORAL_RANGE");
        }
        return date;
    }

    // --- GENERIC ---

    public static void ensure(boolean condition, String message, String errorCode, String ruleName) {
        if (!condition) fail(message, errorCode, ruleName);
    }

    // Config setter for dynamic updates
    public static void setConfig(DomainGuardConfig newConfig) {
        config = newConfig;
    }
}
