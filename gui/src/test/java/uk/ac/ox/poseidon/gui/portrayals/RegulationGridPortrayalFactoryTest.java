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

package uk.ac.ox.poseidon.gui.portrayals;

import org.junit.jupiter.api.Test;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.regulations.Regulations;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegulationGridPortrayalFactoryTest {

    /**
     * This test method verifies the `newInstance` method of the RegulationGridPortrayalFactory. The
     * tested method creates a new instance of ObjectGridPortrayal2D (specifically a subclass) based
     * on the Simulation provided, using injected regulatory, fleet, and bathymetric context.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testNewInstance_CreatesNonNullPortrayal() {
        // Arrange
        final SimulationScope scope = mock(SimulationScope.class);
        final Regulations<Vessel> mockRegulations = mock(Regulations.class);
        final VesselField mockVesselField = mock(VesselField.class);
        final BathymetricGrid mockBathymetricGrid = mock(BathymetricGrid.class);

        final Factory<SimulationScope, Regulations<Vessel>> regulationsFactory =
            mock(Factory.class);
        when(regulationsFactory.get(scope)).thenReturn(mockRegulations);

        final Factory<SimulationScope, VesselField> vesselsFactory = mock(Factory.class);
        when(vesselsFactory.get(scope)).thenReturn(mockVesselField);

        final DoubleGrid2D mockField = new DoubleGrid2D(10, 20);
        when(mockBathymetricGrid.getField()).thenReturn(mockField);

        final Factory<SimulationScope, BathymetricGrid> bathymetricFactory = mock(Factory.class);
        when(bathymetricFactory.get(scope)).thenReturn(mockBathymetricGrid);

        final Factory<SimulationScope, Gear> gearFactory = mock(Factory.class);
        final Gear mockGear = mock(Gear.class);
        when(gearFactory.get(scope)).thenReturn(mockGear);

        final RegulationGridPortrayalFactory factory = new RegulationGridPortrayalFactory(
            regulationsFactory,
            vesselsFactory,
            bathymetricFactory,
            800,
            600
        );

        // Act
        final ObjectGridPortrayal2D portrayal = factory.newInstance(scope);

        // Assert
        assertNotNull(portrayal, "The newInstance method should return a non-null instance.");
        assertInstanceOf(
            RegulationGridPortrayalFactory.Portrayal.class,
            portrayal,
            "The newInstance method should return an instance of RegulationGridPortrayalFactory" +
                ".Portrayal"
        );
    }
}
