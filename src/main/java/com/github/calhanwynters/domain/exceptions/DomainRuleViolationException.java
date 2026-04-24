package com.github.calhanwynters.domain.exceptions;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Standard domain exception for 2026 Architectures.
 * Signals business rule violations with metadata for telemetry and API responses.
 */
public class DomainRuleViolationException extends RuntimeException {

    private final String errorCode;
    private final String ruleName;
    private final Instant timestamp;

    public DomainRuleViolationException(String message, String errorCode, String ruleName) {
        super(validateMessage(message));
        this.errorCode = errorCode;
        this.ruleName = ruleName;
        this.timestamp = Instant.now();
    }

    public DomainRuleViolationException(String message, Throwable cause, String errorCode, String ruleName) {
        super(validateMessage(message), cause);
        this.errorCode = errorCode;
        this.ruleName = ruleName;
        this.timestamp = Instant.now();
    }

    private static String validateMessage(String message) {
        if (Objects.requireNonNull(message, "Message cannot be null").isBlank()) {
            throw new IllegalArgumentException("Exception message cannot be blank");
        }
        return message;
    }

    public Optional<String> getErrorCode() {
        return Optional.ofNullable(errorCode);
    }

    public Optional<String> getRuleName() {
        return Optional.ofNullable(ruleName);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a formatted summary suitable for internal logs or OpenTelemetry spans.
     */
    public String getDiagnosticSummary() {
        return "[%s] [%s] Rule '%s' failed: %s".formatted(
                timestamp,
                getErrorCode().orElse("UNKNOWN_CODE"),
                getRuleName().orElse("GENERIC_RULE"),
                getMessage()
        );
    }
}
