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

package uk.ac.ox.poseidon.geography.bathymetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.ModelGridFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class BathymetricGridFromElevationValuesFactoryTest {

    final Simulation simulation = mock(Simulation.class);
    BathymetricGridFromElevationValuesFactory factory;

    @BeforeEach
    void setUp() {
        factory = new BathymetricGridFromElevationValuesFactory();
        factory.setModelGrid(
            new ModelGridFactory(1, -1, 1, -1, 1)
        );
    }

    @Test
    void elevationsValuesMustNotBeNull() {
        assertThrows(
            NullPointerException.class,
            () -> factory.newInstance(simulation)
        );
    }

    @Test
    void incorrectNumberOfElevationValuesFails() {
        Stream
            .of(0, 3, 5)
            .map(n -> Stream.generate(() -> 0.0).limit(n).toList())
            .forEach(elevationValues -> {
                factory.setElevationValues(List.of(0.0, 0.0, 0.0));
                assertThrows(
                    IllegalArgumentException.class,
                    () -> factory.newInstance(simulation)
                );
            });
    }

    @Test
    void correctNumberOfElevationValuesSucceeds() {
        factory.setElevationValues(List.of(0.0, 0.0, 0.0, 0.0));
        factory.newInstance(simulation);
    }

    @Test
    void valuesAreMappedIntuitively() {
        factory.setElevationValues(List.of(
            0.0, 1.0,
            2.0, 3.0
        ));
        final BathymetricGrid bathymetricGrid = factory.newInstance(simulation);
        assertEquals(0.0, bathymetricGrid.getElevation(new Int2D(0, 0)));
        assertEquals(1.0, bathymetricGrid.getElevation(new Int2D(1, 0)));
        assertEquals(2.0, bathymetricGrid.getElevation(new Int2D(0, 1)));
        assertEquals(3.0, bathymetricGrid.getElevation(new Int2D(1, 1)));
    }
}
