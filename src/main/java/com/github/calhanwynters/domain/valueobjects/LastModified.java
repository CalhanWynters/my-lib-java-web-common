package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Value Object for last modification timestamps.
 * Aligned with DomainGuard for 2026 Edition.
 */
public record LastModified(OffsetDateTime value) {

    // Boundary: Logical lower bound (System Epoch)
    private static final OffsetDateTime MIN_SYSTEM_DATE = OffsetDateTime.parse("2025-01-01T00:00:00Z");

    /**
     * Compact constructor using DomainGuard for temporal invariants.
     */
    public LastModified {
        // 1. Existence
        DomainGuard.notNull(value, "Last Modified Date");

        // 2. Truncation (Normalize precision to prevent DB mismatch)
        value = value.truncatedTo(ChronoUnit.MICROS);

        // 3. Logical Range (Far Past)
        DomainGuard.ensure(
                !value.isBefore(MIN_SYSTEM_DATE),
                "Date is before system epoch (%s)".formatted(MIN_SYSTEM_DATE),
                "VAL-015", "TEMPORAL_RANGE"
        );

        // 4. Future check (utilizing DomainGuard's 500ms drift tolerance)
        // We convert to Instant to use the centralized drift logic
        DomainGuard.inPast(value.toInstant(), "Last Modified Date");
    }

    /**
     * Factory method using system UTC clock.
     */
    public static LastModified now() {
        return new LastModified(OffsetDateTime.now(Clock.systemUTC()));
    }

    /**
     * Testing/Mocking Factory: Allows injection of fixed clocks.
     */
    public static LastModified now(Clock clock) {
        return new LastModified(OffsetDateTime.now(clock));
    }
}
