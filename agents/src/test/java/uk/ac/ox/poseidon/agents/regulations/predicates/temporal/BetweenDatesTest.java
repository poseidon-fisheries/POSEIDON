/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.regulations.predicates.temporal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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
        assertThrows(IllegalArgumentException.class, () -> new BetweenDates(startDate, endDate));
    }

    @Test
    void testActionWithinDateRangeStartOnly() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 6, 15, 12, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2024, 1, 1, 12, 0));

        // Act & Assert
        assertTrue(betweenDates.test(action));
    }

    @Test
    void testActionWithinDateRangeEndOnly() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2022, 12, 31, 12, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 6, 15, 12, 0));

        // Act & Assert
        assertTrue(betweenDates.test(action));
    }

    @Test
    void testActionOutsideDateRange() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2022, 12, 31, 12, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2024, 1, 1, 12, 0));

        // Act & Assert
        assertFalse(betweenDates.test(action));
    }

    @Test
    void testActionExactlyOnStartDate() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 1, 1, 0, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 12, 31, 23, 59));

        // Act & Assert
        assertTrue(betweenDates.test(action));
    }

    @Test
    void testActionExactlyOnEndDate() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2022, 12, 31, 23, 59));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 12, 31, 0, 0));

        // Act & Assert
        assertTrue(betweenDates.test(action));
    }

    @Test
    void testActionBeforeDateRange() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2022, 12, 30, 12, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2022, 12, 31, 23, 59));

        // Act & Assert
        assertFalse(betweenDates.test(action));
    }

    @Test
    void testActionAfterDateRange() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2024, 1, 2, 12, 0));

        // Act & Assert
        assertFalse(betweenDates.test(action));
    }

    @Test
    void testActionNullEndDateWithinRange() {
        // Arrange
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final BetweenDates betweenDates = new BetweenDates(startDate, endDate);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 6, 15, 12, 0));
        Mockito.when(action.getEnd()).thenReturn(null);

        // Act & Assert
        assertTrue(betweenDates.test(action));
    }
}
