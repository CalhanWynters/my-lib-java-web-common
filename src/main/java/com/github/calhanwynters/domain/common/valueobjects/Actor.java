package com.github.calhanwynters.domain.common.valueobjects;

import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;
import jakarta.persistence.Embeddable;

import java.util.Collections;
import java.util.Set;

@Embeddable
public record Actor(String identity, Set<String> roles) {

    // SOC 2 Win: Define the "Internal System" actor with no roles (or specific system roles)
    public static final Actor SYSTEM = new Actor("INTERNAL_SYSTEM", Collections.emptySet());

    public static final String ROLE_ADMIN = "ROLE_PRODUCT_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_CATALOG_MANAGER";

    public Actor {
        identity = DomainGuard.notBlank(identity, "Actor Identity");
        roles = DomainGuard.notNull(roles, "Actor Roles");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public static Actor of(String identity, Set<String> roles) {
        return new Actor(identity, roles);
    }
}
