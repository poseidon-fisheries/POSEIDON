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
import uk.ac.ox.poseidon.core.Simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InYearFactoryTest {

    /**
     * The {@code InYearFactory} class is responsible for creating instances of the {@code InYear}
     * class. The {@code newInstance} method uses the provided {@code Simulation} instance to create
     * an {@code InYear} object with the factory's current {@code year} value.
     */

    @Test
    void testNewInstance_WithValidYear_ReturnsInYearInstance() {
        // Arrange
        final int year = 2023;
        final InYearFactory factory = new InYearFactory(year);

        final Simulation simulation = new Simulation();

        // Act
        final InYear result = factory.newInstance(simulation);

        // Assert
        assertEquals(year, result.getYear());
    }
}
