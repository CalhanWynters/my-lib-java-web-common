package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Pattern;

public record ImageUrl(String url) {

    private static final int MAX_URL_LENGTH = 2048;
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private static final Pattern URL_SAFE_PATTERN =
            Pattern.compile("^[a-zA-Z0-9:/\\-._~%?#\\[\\]!$&'()*+,;=]+$");

    private static final Pattern INTERNAL_HOST_PATTERN = Pattern.compile(
            "^(localhost|127\\.|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.|169\\.254\\.|0\\.0\\.0\\.0).*",
            Pattern.CASE_INSENSITIVE
    );

    public ImageUrl {
        DomainGuard.notBlank(url, "Image URL");
        String normalized = url.strip();

        // Ensure DomainGuard has an overload for min/max parameters
        // or replace with a custom check using DomainGuard.ensure
        if (normalized.length() > MAX_URL_LENGTH) {
            DomainGuard.ensure(false, "URL exceeds max length.", "VAL-002", "SIZE");
        }

        DomainGuard.matches(normalized, URL_SAFE_PATTERN, "Image URL");

        String host = extractAndValidateHost(normalized);

        DomainGuard.ensure(
                !INTERNAL_HOST_PATTERN.matcher(host).matches(),
                "URL points to a restricted internal network.",
                "VAL-017", "SECURITY_SSRF"
        );

        url = normalized;
    }

    private static String extractAndValidateHost(String rawUrl) {
        try {
            URI uri = new URI(rawUrl);
            String scheme = uri.getScheme();

            DomainGuard.ensure(
                    scheme != null && ALLOWED_SCHEMES.contains(scheme.toLowerCase()),
                    "Invalid scheme: Only HTTPS/HTTP allowed.",
                    "VAL-004", "SYNTAX"
            );

            String host = uri.getHost();
            DomainGuard.ensure(
                    host != null && !host.isBlank(),
                    "URL must contain a valid host.",
                    "VAL-004", "SYNTAX"
            );

            return host;
        } catch (URISyntaxException e) {
            // Effectively throwing VAL-004
            DomainGuard.ensure(false, "Malformed URL: " + e.getReason(), "VAL-004", "SYNTAX");
            return "";
        }
    }
}
