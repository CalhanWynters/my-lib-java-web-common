package com.github.calhanwynters.domain.common.exceptions;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Standard domain exception for 2026 Architectures.
 * Signals business rule violations with metadata for telemetry and API responses.
 */
public class DomainRuleViolationException extends RuntimeException {

    private final String errorCode;
    private final String violatedRule;
    private final Instant timestamp;

    public DomainRuleViolationException(String message, String errorCode, String violatedRule) {
        super(validateMessage(message));
        this.errorCode = errorCode;
        this.violatedRule = violatedRule;
        this.timestamp = Instant.now();
    }

    public DomainRuleViolationException(String message, Throwable cause, String errorCode, String violatedRule) {
        super(validateMessage(message), cause);
        this.errorCode = errorCode;
        this.violatedRule = violatedRule;
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

    public Optional<String> getViolatedRule() {
        return Optional.ofNullable(violatedRule);
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
                getViolatedRule().orElse("GENERIC_RULE"),
                getMessage()
        );
    }
}
