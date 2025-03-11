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

package uk.ac.ox.poseidon.geography.predicates;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Envelope;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class InRectangularAreaPredicateFactoryTest {

    /**
     * Test class for the InRectangularPredicateAreaFactory. Verifies the behavior of the
     * newInstance method under different scenarios.
     */

    @Test
    void testNewInstance_createsPredicateSuccessfully() {
        // Arrange
        final Simulation mockSimulation = Mockito.mock(Simulation.class);
        final Envelope mockEnvelope = Mockito.mock(Envelope.class);

        @SuppressWarnings("unchecked") final Factory<Envelope> mockFactory =
            (Factory<Envelope>) Mockito.mock(Factory.class);

        when(mockFactory.get(mockSimulation)).thenReturn(mockEnvelope);

        final InRectangularAreaPredicateFactory factory = new InRectangularAreaPredicateFactory(
            mockFactory);

        // Act
        final InRectangularAreaPredicate predicate = factory.newInstance(mockSimulation);

        // Assert
        assertNotNull(predicate, "The newInstance method should return a non-null predicate.");
    }
}
