package com.github.calhanwynters.domain.common.valueobjects;

import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Hardened Image URL Value Object for 2026 Edition.
 * Audited for SSRF prevention and DoS resilience via DomainGuard.
 */
public record ImageUrl(String url) {

    private static final int MAX_URL_LENGTH = 2048;
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    // Lexical Whitelist: Disallows '@' to prevent User-Info SSRF obfuscation
    private static final Pattern URL_SAFE_PATTERN =
            Pattern.compile("^[a-zA-Z0-9:/\\-._~%?#\\[\\]!$&'()*+,;=]+$");

    // Structural SSRF Block: Restricted internal network patterns
    private static final Pattern INTERNAL_HOST_PATTERN = Pattern.compile(
            "^(localhost|127\\.|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.|169\\.254\\.|0\\.0\\.0\\.0).*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Compact Constructor enforcing URL invariants and SSRF boundaries.
     */
    public ImageUrl {
        // 1. Existence and initial content (Throws VAL-010)
        DomainGuard.notBlank(url, "Image URL");

        // 2. Normalization
        String normalized = url.strip();

        // 3. Size & Boundary (Throws VAL-002)
        DomainGuard.lengthBetween(normalized, 1, MAX_URL_LENGTH, "Image URL");

        // 4. Lexical Content (Throws VAL-004)
        DomainGuard.matches(normalized, URL_SAFE_PATTERN, "Image URL");

        // 5. Syntax & Semantics
        String host = extractAndValidateHost(normalized);

        // 6. Security: SSRF Block (Throws VAL-017 for Security violations)
        DomainGuard.ensure(
                !INTERNAL_HOST_PATTERN.matcher(host).matches(),
                "URL points to a restricted internal network.",
                "VAL-017", "SECURITY_SSRF"
        );

        // Assignment
        url = normalized;
    }

    /**
     * Parses URI and returns a validated host using DomainGuard for structural checks.
     */
    private static String extractAndValidateHost(String rawUrl) {
        try {
            URI uri = new URI(rawUrl);

            // 1. Validate Scheme
            String scheme = uri.getScheme();
            DomainGuard.ensure(
                    scheme != null && ALLOWED_SCHEMES.contains(scheme.toLowerCase()),
                    "Invalid scheme: Only HTTPS/HTTP allowed.",
                    "VAL-004", "SYNTAX"
            );

            // 2. Extract and Validate Host (Resolving the 'might be null' warning)
            String host = uri.getHost();

            // This guard ensures 'host' is non-null and non-blank for the rest of the flow
            DomainGuard.ensure(
                    host != null && !host.isBlank(),
                    "URL must contain a valid host.",
                    "VAL-004", "SYNTAX"
            );

            return host;
        } catch (URISyntaxException e) {
            DomainGuard.ensure(
                    false,
                    "Malformed URL syntax: " + e.getReason(),
                    "VAL-004", "SYNTAX"
            );
            return ""; // Unreachable but satisfies compiler
        }
    }
}
