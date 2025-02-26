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

package uk.ac.ox.poseidon.core.predicates.numeric;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AboveFactoryTest {

    /**
     * Unit tests for AboveFactory class.
     * <p>
     * The AboveFactory class is a factory responsible for creating instances of the Above class.
     * The `newInstance` method of AboveFactory takes a Simulation object and produces a new Above
     * instance. Instances of Above require a threshold and a function to extract double values from
     * Objects.
     */

    @Test
    void testNewInstance_CreatesAboveInstanceCorrectly() {
        // Arrange
        final double threshold = 10.0;
        final Simulation simulation = mock(Simulation.class);

        @SuppressWarnings("unchecked") final Factory<ToDoubleFunction<Object>>
            doubleFunctionFactory = mock(Factory.class);
        final ToDoubleFunction<Object> doubleFunction = mock(ToDoubleFunction.class);

        when(doubleFunctionFactory.get(simulation)).thenReturn(doubleFunction);

        final AboveFactory aboveFactory = new AboveFactory(threshold, doubleFunctionFactory);

        // Act
        final Above createdAbove = aboveFactory.newInstance(simulation);

        // Assert
        assertNotNull(createdAbove);
        verify(doubleFunctionFactory, times(1)).get(simulation);
    }
}
