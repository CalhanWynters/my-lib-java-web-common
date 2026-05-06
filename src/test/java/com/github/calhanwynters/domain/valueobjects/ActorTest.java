package com.github.calhanwynters.domain.valueobjects;

import com.github.calhanwynters.domain.exceptions.DomainRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ActorTest {

    @Test
    @DisplayName("Should reject Actor when role is not in the whitelist")
    void invalidRoleWhitelisting() {
        Set<String> badRoles = Set.of("ROLE_UNAUTHORIZED_USER");

        assertThatThrownBy(() -> new Actor("user-123", badRoles))
                .isInstanceOf(DomainRuleViolationException.class)
                .satisfies(ex -> {
                    var e = (DomainRuleViolationException) ex;
                    assertThat(e.getErrorCode()).hasValue("VAL-020");
                    assertThat(e.getRuleName()).hasValue("LEXICAL_CONTENT");
                });
    }
}
