package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Hardened Creation Timestamp Value Object.
 * Enforces system-wide temporal boundaries and microsecond precision.
 */
public record CreatedAt(OffsetDateTime value) {

    private static final OffsetDateTime MIN_SYSTEM_DATE = OffsetDateTime.parse("2025-01-01T00:00:00Z");

    /**
     * Compact Constructor following the Fail-Fast Hierarchy.
     */
    public CreatedAt {
        // 1. EXISTENCE
        DomainGuard.notNull(value, "Created At Date");

        // 2. NORMALIZATION (Enforce DB/API precision standards)
        value = value.truncatedTo(ChronoUnit.MICROS);

        // 3. SEMANTICS: Boundary Check
        DomainGuard.ensure(
                !value.isBefore(MIN_SYSTEM_DATE),
                "Creation date cannot be before system launch (received: %s).".formatted(value),
                "VAL-015", "TEMPORAL_RANGE"
        );

        // 4. TEMPORAL: Real-time Validity (Handles Clock Drift)
        DomainGuard.inPast(value.toInstant(), "Created At Date");
    }

    public static CreatedAt now() {
        return new CreatedAt(OffsetDateTime.now(Clock.systemUTC()));
    }

    /**
     * Testing-friendly factory to allow for deterministic validation checks.
     */
    public static CreatedAt now(Clock clock) {
        return new CreatedAt(OffsetDateTime.now(clock));
    }
}
