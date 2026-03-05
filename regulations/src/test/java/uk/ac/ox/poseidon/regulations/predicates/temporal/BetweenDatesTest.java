/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.regulations.predicates.temporal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.threeten.extra.Interval;
import uk.ac.ox.poseidon.regulations.TemporalAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static java.time.ZoneOffset.UTC;

class BetweenDatesTest {

    /**
     * Tests the {@code BetweenDates} class, which is a predicate that checks whether the start or
     * end date of an {@code Action} falls between a given date range.
     */

    @Test
    void testStartDateAfterEndDateThrowsException() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 12, 31);
        final LocalDate endDate = LocalDate.of(2023, 1, 1);

        // Act & Assert
        assertThatThrownBy(() -> new BetweenDates(startDate, endDate))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testActionWithinDateRangeStartOnly() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2023, 6, 15, 12, 0),
            LocalDateTime.of(2024, 1, 1, 12, 0)
        );

        // Act & Assert
        assertThat(betweenDates.test(action)).isTrue();
    }

    @Test
    void testActionWithinDateRangeEndOnly() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2022, 12, 31, 12, 0),
            LocalDateTime.of(2023, 6, 15, 12, 0)
        );

        // Act & Assert
        assertThat(betweenDates.test(action)).isTrue();
    }

    @Test
    void testActionExactlyOnStartDate() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2023, 1, 1, 0, 0),
            LocalDateTime.of(2023, 12, 31, 23, 59)
        );

        // Act & Assert
        assertThat(betweenDates.test(action)).isTrue();
    }

    @Test
    void testActionExactlyOnEndDate() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2022, 12, 31, 23, 59),
            LocalDateTime.of(2023, 12, 31, 0, 0)
        );

        // Act & Assert
        assertThat(betweenDates.test(action)).isTrue();
    }

    @Test
    void testActionBeforeDateRange() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2022, 12, 30, 12, 0),
            LocalDateTime.of(2022, 12, 31, 23, 59)
        );

        // Act & Assert
        assertThat(betweenDates.test(action)).isFalse();
    }

    @Test
    void testActionAfterDateRange() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2024, 1, 1, 0, 0),
            LocalDateTime.of(2024, 1, 2, 12, 0)
        );

        // Act & Assert
        assertThat(betweenDates.test(action)).isFalse();
    }

    @Test
    void testActionStartAndEndDatesWithinRange() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2023, 6, 1, 0, 0),
            LocalDateTime.of(2023, 6, 30, 23, 59)
        );

        // Act & Assert
        assertThat(betweenDates.test(action)).isTrue();
    }

    @Test
    void testActionStartAndEndSpanningDateRange() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2022, 12, 31, 12, 0),
            LocalDateTime.of(2024, 1, 1, 12, 0)
        );

        // Act & Assert
        assertThat(betweenDates.test(action)).isTrue();
    }

    @Test
    void testActionEndingExactlyAtRangeStartDoesNotMatch() {
        final BetweenDates betweenDates = new BetweenDates(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 12, 31)
        );

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2022, 12, 31, 23, 0),
            LocalDateTime.of(2023, 1, 1, 0, 0)
        );

        assertThat(betweenDates.test(action)).isFalse();
    }

    @Test
    void testActionStartingExactlyAtDayAfterRangeEndDoesNotMatch() {
        final BetweenDates betweenDates = new BetweenDates(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 12, 31)
        );

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2024, 1, 1, 0, 0),
            LocalDateTime.of(2024, 1, 1, 1, 0)
        );

        assertThat(betweenDates.test(action)).isFalse();
    }

    @Test
    void testZeroDurationActionInsideRangeMatches() {
        final BetweenDates betweenDates = new BetweenDates(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 12, 31)
        );

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2023, 6, 1, 12, 0),
            LocalDateTime.of(2023, 6, 1, 12, 0)
        );

        assertThat(betweenDates.test(action)).isTrue();
    }

    @Test
    void testZeroDurationActionOutsideRangeDoesNotMatch() {
        final BetweenDates betweenDates = new BetweenDates(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 12, 31)
        );

        final TemporalAction<?> action = mockTemporalAction(
            LocalDateTime.of(2024, 1, 1, 12, 0),
            LocalDateTime.of(2024, 1, 1, 12, 0)
        );

        assertThat(betweenDates.test(action)).isFalse();
    }

    private static TemporalAction<?> mockTemporalAction(
        final LocalDateTime startDateTime,
        final LocalDateTime endDateTime
    ) {
        final TemporalAction<?> action = Mockito.mock(TemporalAction.class);
        Mockito.when(action.getInterval()).thenReturn(
            Interval.of(startDateTime.toInstant(UTC), endDateTime.toInstant(UTC))
        );
        return action;
    }
}
