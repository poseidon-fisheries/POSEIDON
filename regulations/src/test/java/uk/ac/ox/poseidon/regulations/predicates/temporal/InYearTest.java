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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InYearTest {

    /**
     * Tests the {@code test} method of the {@code InYear} class, which evaluates whether the start
     * or end year of an {@code Action} matches the specified year.
     */

    @Test
    void testActionStartYearMatches() {
        // Arrange
        final int targetYear = 2023;
        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 5, 15, 12, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2024, 5, 15, 12, 0));

        final InYear inYear = new InYear(targetYear);

        // Act
        final boolean result = inYear.test(action);

        // Assert
        assertTrue(result);
    }

    @Test
    void testActionEndYearMatches() {
        // Arrange
        final int targetYear = 2023;
        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2022, 5, 15, 12, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 5, 15, 12, 0));

        final InYear inYear = new InYear(targetYear);

        // Act
        final boolean result = inYear.test(action);

        // Assert
        assertTrue(result);
    }

    @Test
    void testBothStartAndEndYearDoNotMatch() {
        // Arrange
        final int targetYear = 2023;
        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2022, 5, 15, 12, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2024, 5, 15, 12, 0));

        final InYear inYear = new InYear(targetYear);

        // Act
        final boolean result = inYear.test(action);

        // Assert
        assertFalse(result);
    }

    @Test
    void testBothStartAndEndYearMatch() {
        // Arrange
        final int targetYear = 2023;
        final Action action = Mockito.mock(Action.class);
        Mockito.when(action.getStart()).thenReturn(LocalDateTime.of(2023, 1, 1, 0, 0));
        Mockito.when(action.getEnd()).thenReturn(LocalDateTime.of(2023, 12, 31, 23, 59));

        final InYear inYear = new InYear(targetYear);

        // Act
        final boolean result = inYear.test(action);

        // Assert
        assertTrue(result);
    }
}
