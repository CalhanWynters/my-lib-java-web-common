package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;

/**
 * Hardened Version Value Object for Java 21/25 (2026 Edition).
 * Aligned with DomainGuard for overflow protection and standardized validation.
 */
public record Version(int value) {

    private static final int MIN_VERSION = 1;
    private static final int MAX_VERSION = 1_000_000;

    public static final Version INITIAL = new Version(MIN_VERSION);

    /**
     * Static factory method for parsing semantic strings.
     */
    public static Version from(String versionString) {
        DomainGuard.notBlank(versionString, "Version String");

        try {
            String majorPart = versionString.split("\\.")[0];
            int major = Integer.parseInt(majorPart);
            return new Version(major);
        } catch (Exception e) {
            DomainGuard.ensure(
                    false,
                    "Invalid version format: " + versionString,
                    "VAL-004", "SYNTAX"
            );
            return null; // Unreachable
        }
    }

    /**
     * Compact Constructor enforcing range boundaries.
     */
    public Version {
        // Throws VAL-007 (RANGE)
        DomainGuard.range(value, MIN_VERSION, MAX_VERSION, "Version");
    }

    /**
     * Creates the next sequential version with Overflow Protection.
     */
    public Version next() {
        DomainGuard.ensure(
                this.value < MAX_VERSION,
                "Maximum version depth reached. Schema rotation required.",
                "VAL-016", "DOMAIN_OVERFLOW"
        );
        return new Version(this.value + 1);
    }

    /**
     * Utility for adapting legacy data (Always-Valid pattern).
     */
    public static Version of(Integer rawValue) {
        if (rawValue == null || rawValue < MIN_VERSION) {
            return INITIAL;
        }
        return new Version(rawValue);
    }
}
