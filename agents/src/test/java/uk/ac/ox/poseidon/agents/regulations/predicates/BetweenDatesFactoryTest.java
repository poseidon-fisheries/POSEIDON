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

package uk.ac.ox.poseidon.agents.regulations.predicates;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BetweenDatesFactoryTest {

    /**
     * Test class for {@link BetweenDatesFactory}.
     *
     * This class contains tests for the `newInstance` method of `BetweenDatesFactory`.
     * The `newInstance` method creates a new {@link BetweenDates} object
     * using the start and end dates obtained from the provided {@link Simulation}.
     */

    /**
     * Test when `newInstance` method successfully creates a `BetweenDates` object with valid start
     * and end dates.
     */
    @Test
    void testNewInstance_createsBetweenDatesWithValidDates() {
        // Arrange
        final LocalDate startDateStub = LocalDate.of(2023, 1, 1);
        final LocalDate endDateStub = LocalDate.of(2023, 12, 31);

        final Factory<LocalDate> startDateFactory = mock(Factory.class);
        final Factory<LocalDate> endDateFactory = mock(Factory.class);
        final Simulation mockSimulation = mock(Simulation.class);

        when(startDateFactory.get(mockSimulation)).thenReturn(startDateStub);
        when(endDateFactory.get(mockSimulation)).thenReturn(endDateStub);

        final BetweenDatesFactory factory = new BetweenDatesFactory(
            startDateFactory,
            endDateFactory
        );

        // Act
        final BetweenDates result = factory.newInstance(mockSimulation);

        // Assert
        assertEquals(
            startDateStub,
            result.getStart(),
            "Start date should match the provided value."
        );
        assertEquals(endDateStub, result.getEnd(), "End date should match the provided value.");
    }

    /**
     * Test when `newInstance` method is invoked and startDateFactory returns null.
     */
    @Test
    void testNewInstance_startDateFactoryReturnsNull() {
        // Arrange
        final LocalDate endDateStub = LocalDate.of(2023, 12, 31);

        final Factory<LocalDate> startDateFactory = mock(Factory.class);
        final Factory<LocalDate> endDateFactory = mock(Factory.class);
        final Simulation mockSimulation = mock(Simulation.class);

        when(startDateFactory.get(mockSimulation)).thenReturn(null);
        when(endDateFactory.get(mockSimulation)).thenReturn(endDateStub);

        final BetweenDatesFactory factory = new BetweenDatesFactory(
            startDateFactory,
            endDateFactory
        );

        // Act & Assert
        try {
            factory.newInstance(mockSimulation);
        } catch (final NullPointerException e) {
            assertEquals(
                "start is marked non-null but is null",
                e.getMessage(),
                "Expected NullPointerException for null start date."
            );
        }
    }

    /**
     * Test when `newInstance` method is invoked and endDateFactory returns null.
     */
    @Test
    void testNewInstance_endDateFactoryReturnsNull() {
        // Arrange
        final LocalDate startDateStub = LocalDate.of(2023, 1, 1);

        final Factory<LocalDate> startDateFactory = mock(Factory.class);
        final Factory<LocalDate> endDateFactory = mock(Factory.class);
        final Simulation mockSimulation = mock(Simulation.class);

        when(startDateFactory.get(mockSimulation)).thenReturn(startDateStub);
        when(endDateFactory.get(mockSimulation)).thenReturn(null);

        final BetweenDatesFactory factory = new BetweenDatesFactory(
            startDateFactory,
            endDateFactory
        );

        // Act & Assert
        try {
            factory.newInstance(mockSimulation);
        } catch (final NullPointerException e) {
            assertEquals(
                "end is marked non-null but is null",
                e.getMessage(),
                "Expected NullPointerException for null end date."
            );
        }
    }

    /**
     * Test to verify that `newInstance` method calls `get` on both startDateFactory and
     * endDateFactory.
     */
    @Test
    void testNewInstance_callsGetOnFactories() {
        // Arrange
        final Factory<LocalDate> startDateFactory = mock(Factory.class);
        when(startDateFactory.get(Mockito.any())).thenReturn(LocalDate.now());
        final Factory<LocalDate> endDateFactory = mock(Factory.class);
        when(endDateFactory.get(Mockito.any())).thenReturn(LocalDate.now());
        final Simulation mockSimulation = mock(Simulation.class);

        final BetweenDatesFactory factory = new BetweenDatesFactory(
            startDateFactory,
            endDateFactory
        );

        // Act
        factory.newInstance(mockSimulation);

        // Assert
        Mockito.verify(startDateFactory).get(mockSimulation);
        Mockito.verify(endDateFactory).get(mockSimulation);
    }
}
