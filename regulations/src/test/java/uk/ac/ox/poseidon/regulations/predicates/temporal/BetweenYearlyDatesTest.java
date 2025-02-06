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

package uk.ac.ox.poseidon.regulations.predicates.temporal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.MonthDay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BetweenYearlyDatesTest {

    @Test
    void testActionWithinNonYearSpanningRange() {
        // Arrange
        final MonthDay beginning = MonthDay.of(Month.JUNE, 1);
        final MonthDay end = MonthDay.of(Month.JULY, 31);
        final BetweenYearlyDates predicate = new BetweenYearlyDates(beginning, end);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 6, 15, 10, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 7, 15, 10, 0));

        // Act & Assert
        assertTrue(predicate.test(action));
    }

    @Test
    void testActionOutsideNonYearSpanningRange() {
        // Arrange
        final MonthDay beginning = MonthDay.of(Month.JUNE, 1);
        final MonthDay end = MonthDay.of(Month.JULY, 31);
        final BetweenYearlyDates predicate = new BetweenYearlyDates(beginning, end);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 5, 15, 10, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 5, 20, 10, 0));

        // Act & Assert
        assertFalse(predicate.test(action));
    }

    @Test
    void testActionWithinYearSpanningRange() {
        // Arrange
        final MonthDay beginning = MonthDay.of(Month.NOVEMBER, 1);
        final MonthDay end = MonthDay.of(Month.FEBRUARY, 28);
        final BetweenYearlyDates predicate = new BetweenYearlyDates(beginning, end);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 11, 15, 10, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 12, 15, 10, 0));

        // Act & Assert
        assertTrue(predicate.test(action));
    }

    @Test
    void testActionOutsideYearSpanningRange() {
        // Arrange
        final MonthDay beginning = MonthDay.of(Month.NOVEMBER, 1);
        final MonthDay end = MonthDay.of(Month.FEBRUARY, 28);
        final BetweenYearlyDates predicate = new BetweenYearlyDates(beginning, end);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 7, 15, 10, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 8, 15, 10, 0));

        // Act & Assert
        assertFalse(predicate.test(action));
    }

    @Test
    void testActionSpanningAcrossYearBoundaryWithinRange() {
        // Arrange
        final MonthDay beginning = MonthDay.of(Month.NOVEMBER, 1);
        final MonthDay end = MonthDay.of(Month.FEBRUARY, 28);
        final BetweenYearlyDates predicate = new BetweenYearlyDates(beginning, end);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 12, 15, 10, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2024, 1, 15, 10, 0));

        // Act & Assert
        assertTrue(predicate.test(action));
    }

    @Test
    void testActionSpanningAcrossYearBoundaryOutsideRange() {
        // Arrange
        final MonthDay beginning = MonthDay.of(Month.NOVEMBER, 1);
        final MonthDay end = MonthDay.of(Month.FEBRUARY, 28);
        final BetweenYearlyDates predicate = new BetweenYearlyDates(beginning, end);

        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 6, 15, 10, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 9, 15, 10, 0));

        // Act & Assert
        assertFalse(predicate.test(action));
    }
}
