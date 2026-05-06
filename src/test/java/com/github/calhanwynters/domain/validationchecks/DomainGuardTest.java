package com.github.calhanwynters.domain.validationchecks;

import com.github.calhanwynters.domain.exceptions.DomainRuleViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class DomainGuardTest {

    private static final String FIELD = "testField";

    @BeforeEach
    void setUp() {
        DomainGuardConfig defaultSafeConfig = new DomainGuardConfig();
        // This uses your existing setConfig method to overwrite the "dirty" one
        DomainGuard.setConfig(defaultSafeConfig);
    }


    @Nested
    @DisplayName("Existence & Text Validations")
    class ExistenceAndTextTests {

        @Test
        void notNull_ShouldThrow_WhenValueIsNull() {
            assertThatThrownBy(() -> DomainGuard.notNull(null, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "VAL-001")
                    .hasFieldOrPropertyWithValue("ruleName", "EXISTENCE")
                    .hasMessageContaining(FIELD);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\n"})
        void notBlank_ShouldThrow_WhenStringIsBlank(String value) {
            assertThatThrownBy(() -> DomainGuard.notBlank(value, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("ruleName", "EXISTENCE");
        }

        @Test
        void lengthBetween_ShouldThrow_WhenOutsideBounds() {
            // 1. Configure the bounds
            DomainGuardConfig customConfig = new DomainGuardConfig();
            customConfig.setMinLength(5);
            customConfig.setMaxLength(10);
            DomainGuard.setConfig(customConfig);

            // 2. "abc" has length 3, which is below the minimum of 5
            assertThatThrownBy(() -> DomainGuard.lengthBetween("abc", FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "VAL-002");
        }


        @Test
        void matches_ShouldThrow_WhenRegexFails() {
            // Compile the string into a Pattern object as required by your method signature
            java.util.regex.Pattern numericPattern = java.util.regex.Pattern.compile("^[0-9]+$");

            // "abc" contains letters, so it fails the numeric-only regex
            assertThatThrownBy(() -> DomainGuard.matches("abc", numericPattern, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "VAL-004");
        }

    }

    @Nested
    @DisplayName("Numeric Validations")
    class NumericTests {

        @Test
        void range_ShouldThrow_WhenValueIsOutOfBounds() {
            assertThatThrownBy(() -> DomainGuard.range(15, 1, 10, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "VAL-007");
        }

        @Test
        @DisplayName("positiveGeneric should fail for zero and negative values")
        void positiveGeneric_ShouldFailForZeroOrNegative() {
            // Zero should fail
            assertThatThrownBy(() -> DomainGuard.positiveGeneric(0, FIELD))
                    .isInstanceOf(DomainRuleViolationException.class);

            // Negative should fail
            assertThatThrownBy(() -> DomainGuard.positiveGeneric(-1.5, FIELD))
                    .isInstanceOf(DomainRuleViolationException.class);

            // NaN should fail
            assertThatThrownBy(() -> DomainGuard.positiveGeneric(Double.NaN, FIELD))
                    .isInstanceOf(DomainRuleViolationException.class);
        }

        @Test
        @DisplayName("nonNegativeGeneric should allow zero but fail for negative values")
        void nonNegativeGeneric_ShouldAllowZeroButFailForNegative() {
            // Zero should PASS
            assertThat(DomainGuard.nonNegativeGeneric(0, FIELD)).isEqualTo(0);
            assertThat(DomainGuard.nonNegativeGeneric(BigDecimal.ZERO, FIELD)).isEqualTo(BigDecimal.ZERO);

            // Negative should FAIL
            assertThatThrownBy(() -> DomainGuard.nonNegativeGeneric(-1, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "VAL-011");

            // Infinity should FAIL
            assertThatThrownBy(() -> DomainGuard.nonNegativeGeneric(Double.POSITIVE_INFINITY, FIELD))
                    .isInstanceOf(DomainRuleViolationException.class);
        }

        @Test
        void maxScale_ShouldUseConfigSetting() {
            // 1. Create a fresh config instance
            DomainGuardConfig customConfig = new DomainGuardConfig();

            // 2. Set the scale on the instance (not the class)
            customConfig.setMaxScale(2);

            // 3. Apply this config to the Guard
            DomainGuard.setConfig(customConfig);

            BigDecimal invalid = new BigDecimal("10.555"); // 3 decimal places

            assertThatThrownBy(() -> DomainGuard.maxScale(invalid, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("cannot exceed 2 decimal places");
        }
    }


    @Nested
    @DisplayName("Collection Validations")
    class CollectionTests {

        @Test
        void notEmpty_ShouldThrow_WhenListIsEmpty() {
            assertThatThrownBy(() -> DomainGuard.notEmpty(Collections.emptyList(), FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class);
        }

        @Test
        void noNullElements_ShouldThrow_WhenNullPresent() {
            List<String> list = Arrays.asList("Valid", null);
            assertThatThrownBy(() -> DomainGuard.noNullElements(list, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class);
        }

        @Test
        void secureList_ShouldBeADefensiveCopy() {
            List<String> input = new ArrayList<>(List.of("A", "B"));
            List<String> secured = DomainGuard.secureList(input, FIELD);

            input.add("C"); // Modify the original source

            assertThat(secured)
                    .as("The secured list should not change when the original input changes")
                    .containsExactly("A", "B")
                    .doesNotContain("C");
        }

    }

    @Nested
    @DisplayName("Temporal Validations")
    class TemporalTests {

        private final Instant now = Instant.parse("2024-01-01T12:00:00Z");
        private final Clock fixedClock = Clock.fixed(now, ZoneId.of("UTC"));

        @Test
        void inPast_ShouldFail_IfDateIsFuture() {
            // Fix: Create instance and inject via setConfig
            DomainGuardConfig customConfig = new DomainGuardConfig();
            customConfig.setTemporalTolerance(Duration.ZERO);
            DomainGuard.setConfig(customConfig);

            Instant futureDate = now.plus(Duration.ofHours(1));

            assertThatThrownBy(() -> DomainGuard.inPast(futureDate, FIELD, fixedClock))
                    .isExactlyInstanceOf(DomainRuleViolationException.class);
        }

        @Test
        @DisplayName("Temporal check should respect config tolerance")
        void inFuture_ShouldPass_IfWithinTolerance() {
            // Fix: Create instance and inject via setConfig
            DomainGuardConfig customConfig = new DomainGuardConfig();
            customConfig.setTemporalTolerance(Duration.ofMinutes(5));
            DomainGuard.setConfig(customConfig);

            // 2 minutes in the past, but tolerance is 5 minutes
            Instant slightlyPast = now.minus(Duration.ofMinutes(2));

            assertThatCode(() -> DomainGuard.inFuture(slightlyPast, FIELD, fixedClock))
                    .doesNotThrowAnyException();
        }
    }
}
