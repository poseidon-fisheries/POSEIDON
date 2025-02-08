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

package uk.ac.ox.poseidon.regulations.predicates.numeric;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BelowFactoryTest {

    /**
     * Test description: Verifies that the `newInstance` method in the `BelowFactory` class
     * correctly creates instances of the `Below` class with the expected threshold value and double
     * function.
     */
    @Test
    void testNewInstance_CreatesInstanceWithCorrectThresholdAndFunction() {
        // Arrange
        final double expectedThreshold = 5.0;
        final Simulation simulation = mock(Simulation.class);
        @SuppressWarnings("unchecked") final Factory<ToDoubleFunction<Action>>
            mockDoubleFunctionFactory = mock(Factory.class);
        @SuppressWarnings("unchecked") final ToDoubleFunction<Action> mockDoubleFunction = mock(
            ToDoubleFunction.class);

        when(mockDoubleFunctionFactory.get(simulation)).thenReturn(mockDoubleFunction);

        final BelowFactory belowFactory = new BelowFactory();
        belowFactory.setThreshold(expectedThreshold);
        belowFactory.setDoubleFunction(mockDoubleFunctionFactory);

        // Act
        final Below result = belowFactory.newInstance(simulation);

        // Assert
        assertNotNull(result, "The Below instance should not be null.");
        assertEquals(
            expectedThreshold,
            belowFactory.getThreshold(),
            "The threshold value should match the initialized value."
        );
        assertEquals(
            result.getDoubleFunction(),
            mockDoubleFunction,
            "The double function should be properly injected."
        );
        verify(mockDoubleFunctionFactory, times(1)).get(simulation);
    }

    /**
     * Test description: Verifies that the `newInstance` method successfully handles and
     * incorporates a valid simulation object passed to the method.
     */
    @Test
    void testNewInstance_HandlesSimulationCorrectly() {
        // Arrange
        final double threshold = 15.0;
        final Simulation simulation = mock(Simulation.class);
        @SuppressWarnings("unchecked") final Factory<ToDoubleFunction<Action>>
            mockDoubleFunctionFactory = mock(Factory.class);
        @SuppressWarnings("unchecked") final ToDoubleFunction<Action> mockDoubleFunction = mock(
            ToDoubleFunction.class);

        when(mockDoubleFunctionFactory.get(simulation)).thenReturn(mockDoubleFunction);

        final BelowFactory belowFactory = new BelowFactory();
        belowFactory.setThreshold(threshold);
        belowFactory.setDoubleFunction(mockDoubleFunctionFactory);

        // Act
        final Below result = belowFactory.newInstance(simulation);

        // Assert
        assertNotNull(result, "The Below instance should not be null.");
        assertEquals(
            mockDoubleFunction,
            result.getDoubleFunction(),
            "The double function should match the one returned from the factory."
        );
        verify(mockDoubleFunctionFactory, times(1)).get(simulation);
    }

    /**
     * Test description: Verifies that the `newInstance` method throws a NullPointerException when a
     * null simulation object is passed.
     */
    @Test
    void testNewInstance_ThrowsExceptionForNullSimulation() {
        // Arrange
        final BelowFactory belowFactory = new BelowFactory();
        belowFactory.setThreshold(10.0);
        @SuppressWarnings("unchecked") final Factory<ToDoubleFunction<Action>>
            mockDoubleFunctionFactory = mock(Factory.class);
        belowFactory.setDoubleFunction(mockDoubleFunctionFactory);

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> belowFactory.newInstance(null),
            "Expected newInstance to throw NullPointerException for null simulation."
        );
    }

    /**
     * Test description: Verifies that the `newInstance` method handles a negative threshold value
     * without errors.
     */
    @Test
    void testNewInstance_HandlesNegativeThreshold() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        @SuppressWarnings("unchecked") final Factory<ToDoubleFunction<Action>>
            mockDoubleFunctionFactory = mock(Factory.class);
        @SuppressWarnings("unchecked") final ToDoubleFunction<Action> mockDoubleFunction = mock(
            ToDoubleFunction.class);

        when(mockDoubleFunctionFactory.get(simulation)).thenReturn(mockDoubleFunction);

        final BelowFactory belowFactory = new BelowFactory();
        belowFactory.setThreshold(-10.0); // Negative threshold
        belowFactory.setDoubleFunction(mockDoubleFunctionFactory);

        // Act
        final Below result = belowFactory.newInstance(simulation);

        // Assert
        assertNotNull(
            result,
            "The Below instance should not be null, even for a negative threshold."
        );
        assertEquals(
            -10.0,
            belowFactory.getThreshold(),
            "The threshold should match the negative value set."
        );
    }
    
}
