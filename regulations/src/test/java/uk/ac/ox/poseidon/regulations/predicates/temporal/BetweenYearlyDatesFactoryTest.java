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
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.time.MonthDayFactory;

import java.time.MonthDay;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class BetweenYearlyDatesFactoryTest {

    /**
     * Test suite for BetweenYearlyDatesFactory class. The class is responsible for creating
     * BetweenYearlyDates instances based on parsed or predefined start and end dates within a
     * simulation context.
     */

    @Test
    void testNewInstanceWithValidStartAndEndDates() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final MonthDay startDate = MonthDay.of(5, 1);
        final MonthDay endDate = MonthDay.of(10, 1);

        final GlobalScopeFactory<MonthDay> startFactory = mock(GlobalScopeFactory.class);
        final GlobalScopeFactory<MonthDay> endFactory = mock(GlobalScopeFactory.class);

        when(startFactory.get()).thenReturn(startDate);
        when(endFactory.get()).thenReturn(endDate);

        final BetweenYearlyDatesFactory factory = new BetweenYearlyDatesFactory(
            startFactory,
            endFactory
        );

        // Act
        final BetweenYearlyDates result = factory.get();

        // Assert
        assertEquals(startDate, result.getStart());
        assertEquals(endDate, result.getEnd());
    }

    @Test
    void testNewInstanceWithYearSpanningDates() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final MonthDay startDate = MonthDay.of(11, 1);
        final MonthDay endDate = MonthDay.of(2, 28);

        final GlobalScopeFactory<MonthDay> startFactory = mock(GlobalScopeFactory.class);
        final GlobalScopeFactory<MonthDay> endFactory = mock(GlobalScopeFactory.class);

        when(startFactory.get()).thenReturn(startDate);
        when(endFactory.get()).thenReturn(endDate);

        final BetweenYearlyDatesFactory factory = new BetweenYearlyDatesFactory(
            startFactory,
            endFactory
        );

        // Act
        final BetweenYearlyDates result = factory.get();

        // Assert
        assertEquals(startDate, result.getStart());
        assertEquals(endDate, result.getEnd());
    }

    @Test
    void testNewInstanceWithSameStartAndEndDates() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final MonthDay sameDate = MonthDay.of(12, 25);

        final GlobalScopeFactory<MonthDay> startFactory = mock(GlobalScopeFactory.class);
        final GlobalScopeFactory<MonthDay> endFactory = mock(GlobalScopeFactory.class);

        when(startFactory.get()).thenReturn(sameDate);
        when(endFactory.get()).thenReturn(sameDate);

        final BetweenYearlyDatesFactory factory = new BetweenYearlyDatesFactory(
            startFactory,
            endFactory
        );

        // Act
        final BetweenYearlyDates result = factory.get();

        // Assert
        assertEquals(sameDate, result.getStart());
        assertEquals(sameDate, result.getEnd());
    }

    @Test
    void testParseWithValidDates() {
        // Arrange
        final String start = "--07-01";
        final String end = "--09-01";

        // Act
        final BetweenYearlyDatesFactory factory = BetweenYearlyDatesFactory.parse(start, end);

        // Assert
        assertNotNull(factory);
        assertInstanceOf(MonthDayFactory.class, factory.getStart());
        assertInstanceOf(MonthDayFactory.class, factory.getEnd());
        final Simulation simulation = mock(Simulation.class);
        final MonthDay startDate = ((MonthDayFactory) factory.getStart()).get();
        final MonthDay endDate = ((MonthDayFactory) factory.getEnd()).get();

        assertEquals(MonthDay.parse(start), startDate);
        assertEquals(MonthDay.parse(end), endDate);
    }

    @Test
    void testParseWithInvalidStartDate() {
        // Arrange
        final String invalidStart = "invalid-date";
        final String end = "--09-01";

        // Act & Assert
        assertThrows(
            DateTimeParseException.class,
            () -> BetweenYearlyDatesFactory.parse(invalidStart, end)
        );
    }

    @Test
    void testParseWithInvalidEndDate() {
        // Arrange
        final String start = "--05-01";
        final String invalidEnd = "invalid-date";

        // Act & Assert
        assertThrows(
            DateTimeParseException.class,
            () -> BetweenYearlyDatesFactory.parse(start, invalidEnd)
        );
    }

    @Test
    void testParseWithBothInvalidDates() {
        // Arrange
        final String invalidStart = "invalid-date";
        final String invalidEnd = "invalid-date";

        // Act & Assert
        assertThrows(
            DateTimeParseException.class,
            () -> BetweenYearlyDatesFactory.parse(invalidStart, invalidEnd)
        );
    }

    @Test
    void testSetStart() {
        // Arrange
        final GlobalScopeFactory<MonthDay> newStartFactory = mock(GlobalScopeFactory.class);
        final BetweenYearlyDatesFactory factory = new BetweenYearlyDatesFactory();

        // Act
        factory.setStart(newStartFactory);

        // Assert
        assertEquals(newStartFactory, factory.getStart());
    }

    @Test
    void testSetEnd() {
        // Arrange
        final GlobalScopeFactory<MonthDay> newEndFactory = mock(GlobalScopeFactory.class);
        final BetweenYearlyDatesFactory factory = new BetweenYearlyDatesFactory();

        // Act
        factory.setEnd(newEndFactory);

        // Assert
        assertEquals(newEndFactory, factory.getEnd());
    }

    @Test
    void testGetStart() {
        // Arrange
        final GlobalScopeFactory<MonthDay> startFactory = mock(GlobalScopeFactory.class);
        final BetweenYearlyDatesFactory factory = new BetweenYearlyDatesFactory();
        factory.setStart(startFactory);

        // Act & Assert
        assertEquals(startFactory, factory.getStart());
    }

    @Test
    void testGetEnd() {
        // Arrange
        final GlobalScopeFactory<MonthDay> endFactory = mock(GlobalScopeFactory.class);
        final BetweenYearlyDatesFactory factory = new BetweenYearlyDatesFactory();
        factory.setEnd(endFactory);

        // Act & Assert
        assertEquals(endFactory, factory.getEnd());
    }
}
