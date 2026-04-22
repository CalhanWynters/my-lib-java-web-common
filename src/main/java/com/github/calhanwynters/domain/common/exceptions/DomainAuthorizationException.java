package com.github.calhanwynters.domain.common.exceptions;

import com.github.calhanwynters.domain.common.valueobjects.Actor;

/**
 * Thrown when an Actor lacks the required roles or identity-based permissions
 * to perform a domain action. Matches SOC 2 Common Criteria for Security Monitoring.
 */
public class DomainAuthorizationException extends DomainRuleViolationException {

    private final String actorIdentity;

    public DomainAuthorizationException(String message, String errorCode, Actor actor) {
        // We pass "AUTHORIZATION_POLICY" as the violated rule for SOC 2 classification
        super(message, errorCode, "AUTHORIZATION_POLICY");
        this.actorIdentity = actor.identity();
    }

    public String getActorIdentity() {
        return actorIdentity;
    }
}
