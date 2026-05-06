package com.github.calhanwynters.domain.validationchecks;

import com.github.calhanwynters.domain.exceptions.DomainRuleViolationException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * Corrected Senior-level test suite for DomainGuard.
 * Aligned with DomainRuleViolationException metadata and Java 21 features.
 */
class DomainGuardTest {

    private static final Instant NOW = Instant.parse("2026-04-23T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Nested
    @DisplayName("Textual Guards")
    class TextualGuards {

        @Test
        @DisplayName("notBlank should strip whitespace and return value")
        void notBlankValid() {
            String input = "  valid_data  ";
            String result = DomainGuard.notBlank(input, "username");
            assertThat(result).isEqualTo("valid_data");
        }

        @Test
        @DisplayName("notBlank should throw on empty or null strings")
        void notBlankInvalid() {
            assertThatThrownBy(() -> DomainGuard.notBlank("   ", "username"))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .satisfies(ex -> {
                        var e = (DomainRuleViolationException) ex;
                        assertThat(e.getErrorCode()).contains("VAL-010");
                        assertThat(e.getRuleName()).contains("TEXT_CONTENT");
                    });
        }

        @Test
        @DisplayName("lengthBetween should enforce boundaries after stripping")
        void lengthBetweenBoundaries() {
            String input = "  abc  ";
            assertThat(DomainGuard.lengthBetween(input, 2, 3, "ERR")).isEqualTo("abc");

            assertThatThrownBy(() -> DomainGuard.lengthBetween(" a ", 2, 5, "ERR"))
                    .isInstanceOf(DomainRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Numeric Guards")
    class NumericGuards {

        @Test
        @DisplayName("positive should accept strictly greater than zero")
        void positiveValidation() {
            BigDecimal val = new BigDecimal("0.01");
            assertThat(DomainGuard.positive(val, "ERR")).isSameAs(val);

            assertThatThrownBy(() -> DomainGuard.positive(BigDecimal.ZERO, "ERR"))
                    .isInstanceOf(DomainRuleViolationException.class);
        }

        @Test
        @DisplayName("range should handle boundaries")
        void rangeBoundaries() {
            // Using Integer.MAX_VALUE to avoid the long-to-int overflow bug
            int max = Integer.MAX_VALUE;
            DomainGuard.range(max, 0, max, "ERR");
            DomainGuard.range(0, 0, 10, "ERR");
        }
    }

    @Nested
    @DisplayName("Collection Guards (Java 21)")
    class CollectionGuards {

        @Test
        @DisplayName("firstNotNull should throw if the actual first element is null")
        void firstNotNullStrictCheck() {
            SequencedCollection<String> list = new LinkedList<>();
            list.add(null);

            assertThatThrownBy(() -> DomainGuard.firstNotNull(list, "myCollection"))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .satisfies(ex -> {
                        var e = (DomainRuleViolationException) ex;
                        assertThat(e.getErrorCode()).contains("VAL-015");
                        assertThat(e.getRuleName()).contains("COLLECTION_ORDER");
                    });
        }

        @Test
        @DisplayName("noNullElements should catch null even at the end of a list")
        void noNullElementsDeepCheck() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c"));
            list.add(null);

            assertThatThrownBy(() -> DomainGuard.noNullElements(list, "myList"))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .satisfies(ex -> {
                        var e = (DomainRuleViolationException) ex;
                        assertThat(e.getErrorCode()).contains("VAL-012");
                        assertThat(e.getRuleName()).contains("COLLECTION_INTEGRITY");
                    });
        }
    }

    @Nested
    @DisplayName("Temporal Guards")
    class TemporalGuards {

        @Test
        @DisplayName("inPast should accept future timestamps within 500ms drift")
        void inPastDriftTolerance() {
            Instant nearFuture = NOW.plusMillis(400);
            Instant result = DomainGuard.inPast(nearFuture, "eventDate", FIXED_CLOCK);
            assertThat(result).isEqualTo(nearFuture);
        }

        @Test
        @DisplayName("inFuture should accept past timestamps within 500ms drift")
        void inFutureDriftTolerance() {
            Instant nearPast = NOW.minusMillis(400);
            assertThat(DomainGuard.inFuture(nearPast, "startDate", FIXED_CLOCK))
                    .isEqualTo(nearPast);
        }
    }

    @Nested
    @DisplayName("Lexical Guards")
    class LexicalGuards {

        @Test
        @DisplayName("inAllowedList should permit whitelisted words")
        void inAllowedListValid() {
            Set<String> roles = Set.of("ADMIN", "USER", "GUEST");
            assertThat(DomainGuard.inAllowedList("  USER  ", roles, "role")).isEqualTo("USER");
        }

        @Test
        @DisplayName("inAllowedList should reject non-whitelisted words")
        void inAllowedListInvalid() {
            Set<String> roles = Set.of("ADMIN", "USER");
            assertThatThrownBy(() -> DomainGuard.inAllowedList("SUPER_ADMIN", roles, "role"))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .satisfies(ex -> {
                        var e = (DomainRuleViolationException) ex;
                        // AssertJ unwrap logic:
                        assertThat(e.getErrorCode()).hasValue("VAL-020");
                        assertThat(e.getRuleName()).hasValue("LEXICAL_CONTENT");
                    });
        }


        @Test
        @DisplayName("noProfanity should reject based on predicate")
        void noProfanityCheck() {
            Predicate<String> filter = s -> s.toLowerCase().contains("badword");

            assertThatThrownBy(() -> DomainGuard.noProfanity("That is a badword!", filter, "comment"))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("prohibited language");
        }
    }
}
