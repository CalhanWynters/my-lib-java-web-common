package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public record CreatedAt(OffsetDateTime value) {

    private static final OffsetDateTime MIN_SYSTEM_DATE = OffsetDateTime.parse("2025-01-01T00:00:00Z");

    public CreatedAt {
        DomainGuard.notNull(value, "Created At Date");
        value = value.truncatedTo(ChronoUnit.MICROS);

        DomainGuard.ensure(
                !value.isBefore(MIN_SYSTEM_DATE),
                "Creation date cannot be before system launch.",
                "VAL-015", "TEMPORAL_RANGE"
        );

        DomainGuard.inPast(value.toInstant(), "Created At Date");
    }

    public static CreatedAt now() {
        return new CreatedAt(OffsetDateTime.now(Clock.systemUTC()));
    }
}
