package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.validationchecks.AllowedList;
import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import jakarta.persistence.Embeddable;

import java.util.Collections;
import java.util.Set;

@Embeddable
public record Actor(String identity, Set<String> roles) {

    public static final String ROLE_ADMIN = "ROLE_PRODUCT_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_CATALOG_MANAGER";

    /**
     * Whitelist for Lexical Validation.
     * Wrapped in AllowedList to match DomainGuard signature.
     */
    private static final AllowedList ALLOWED_ROLES = new AllowedList(
            Set.of(ROLE_ADMIN, ROLE_MANAGER)
    );

    /**
     * SOC 2 Win: Define the "Internal System" actor.
     * Note: An empty set of roles is valid for the system actor here.
     */
    public static final Actor SYSTEM = new Actor("INTERNAL_SYSTEM", Collections.emptySet());

    /**
     * Canonical Constructor with Domain Guards.
     */
    public Actor {
        // 1. Syntax & Existence
        identity = DomainGuard.notBlank(identity, "Actor Identity");

        // 2. Collection Integrity
        DomainGuard.notNull(roles, "Actor Roles");

        // 3. Lexical Whitelisting (Individual Role Validation)
        // Ensure every role passed is strictly within our allowed vocabulary
        roles.forEach(role ->
                DomainGuard.inAllowedList(role, ALLOWED_ROLES, "Actor Role")
        );

        // 4. Final Security: Defensively copy to maintain immutability
        roles = Set.copyOf(roles);
    }

    /**
     * Helper to check for specific permissions.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Static factory method for cleaner instantiation.
     */
    public static Actor of(String identity, Set<String> roles) {
        return new Actor(identity, roles);
    }
}
