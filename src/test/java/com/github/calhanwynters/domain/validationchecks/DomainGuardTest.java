package com.github.calhanwynters.domain.validationchecks;

import com.github.calhanwynters.domain.exceptions.DomainRuleViolationException;
import org.junit.jupiter.api.*;

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
                    .extracting(e -> ((DomainRuleViolationException) e).getErrorCode().orElseThrow())
                    .isEqualTo("VAL-001");
        }

        @Test
        void notBlank_ShouldThrow_WhenStringIsBlank() {
            assertThatThrownBy(() -> DomainGuard.notBlank("   ", FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    .extracting(e -> ((DomainRuleViolationException) e).getRuleName().orElseThrow())
                    .isEqualTo("TEXT_CONTENT");
        }

        @Test
        void lengthBetween_ShouldThrow_WhenOutsideBounds() {
            assertThatThrownBy(() -> DomainGuard.lengthBetween("abc", 5, 10, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    // .orElseThrow() removes the "might evaluate to null" warning
                    .extracting(e -> ((DomainRuleViolationException) e).getErrorCode().orElseThrow())
                    .isEqualTo("VAL-002");
        }




        @Test
        void matches_ShouldThrow_WhenRegexFails() {
            String pattern = "^[0-9]+$";
            java.util.regex.Pattern numericPattern = java.util.regex.Pattern.compile(pattern);

            assertThatThrownBy(() -> DomainGuard.matches("abc", numericPattern, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    // Use orElseThrow() to satisfy @NotNull requirements and clear IDE warnings
                    .extracting(e -> ((DomainRuleViolationException) e).getErrorCode().orElseThrow())
                    .isEqualTo("VAL-004");
        }



    }

    @Nested
    @DisplayName("Numeric Validations")
    class NumericTests {

        @Test
        void range_ShouldThrow_WhenValueIsOutOfBounds() {
            assertThatThrownBy(() -> DomainGuard.range(15, 1, 10, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    // Use orElseThrow() to remove the "might evaluate to null" warning
                    .extracting(e -> ((DomainRuleViolationException) e).getErrorCode().orElseThrow())
                    .isEqualTo("VAL-007");
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

            // Negative should FAIL
            assertThatThrownBy(() -> DomainGuard.nonNegativeGeneric(-1, FIELD))
                    .isExactlyInstanceOf(DomainRuleViolationException.class)
                    // orElseThrow() ensures a String is returned, silencing the @NotNull warning
                    .extracting(e -> ((DomainRuleViolationException) e).getErrorCode().orElseThrow())
                    .isEqualTo("VAL-011");
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
