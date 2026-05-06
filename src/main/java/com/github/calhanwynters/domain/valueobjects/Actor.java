package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import jakarta.persistence.Embeddable;

import java.util.Collections;
import java.util.Set;

@Embeddable
public record Actor(String identity, Set<String> roles) {

    public static final String ROLE_ADMIN = "ROLE_PRODUCT_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_CATALOG_MANAGER";

    // Whitelist for Lexical Validation
    private static final Set<String> ALLOWED_ROLES = Set.of(ROLE_ADMIN, ROLE_MANAGER);

    // SOC 2 Win: Define the "Internal System" actor
    public static final Actor SYSTEM = new Actor("INTERNAL_SYSTEM", Collections.emptySet());

    public Actor {
        // 1. Syntax & Existence
        identity = DomainGuard.notBlank(identity, "Actor Identity");

        // 2. Collection Integrity
        DomainGuard.notNull(roles, "Actor Roles");

        // 3. Lexical Whitelisting (Individual Role Validation)
        // We ensure every role passed is strictly within our allowed vocabulary
        roles.forEach(role ->
                DomainGuard.inAllowedList(role, ALLOWED_ROLES, "Actor Role")
        );

        // Defensively copy to maintain immutability after validation
        roles = Set.copyOf(roles);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public static Actor of(String identity, Set<String> roles) {
        return new Actor(identity, roles);
    }
}
