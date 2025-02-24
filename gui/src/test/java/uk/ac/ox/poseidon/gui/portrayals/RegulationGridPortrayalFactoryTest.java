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

package uk.ac.ox.poseidon.gui.portrayals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.fishing.DummyFishingAction;
import uk.ac.ox.poseidon.agents.fleets.Fleet;
import uk.ac.ox.poseidon.agents.regulations.Regulations;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.awt.*;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class RegulationGridPortrayalFactoryTest {

    @Test
    void testNewInstanceCreatesNonNullPortrayal() {
        final Factory<Regulations> mockRegulationsFactory = mock(Factory.class);
        final Factory<Fleet> mockFleetFactory = mock(Factory.class);
        final Factory<BathymetricGrid> mockBathymetricGridFactory = mock(Factory.class);

        final Simulation mockSimulation = mock(Simulation.class);
        final TemporalSchedule mockSchedule = mock(TemporalSchedule.class);

        when(mockSimulation.getTemporalSchedule()).thenReturn(mockSchedule);
        when(mockRegulationsFactory.get(mockSimulation)).thenReturn(mock(Regulations.class));
        when(mockFleetFactory.get(mockSimulation)).thenReturn(mock(Fleet.class));
        when(mockBathymetricGridFactory.get(mockSimulation)).thenReturn(mock(BathymetricGrid.class));

        final RegulationGridPortrayalFactory factory = new RegulationGridPortrayalFactory(
            mockRegulationsFactory,
            mockFleetFactory,
            mockBathymetricGridFactory
        );

        assertNotNull(
            factory.newInstance(mockSimulation),
            "Expected newInstance to return a valid Portrayal object"
        );
    }

    @Test
    void testNewInstancePassesCorrectDependencies() {
        final Regulations mockRegulations = mock(Regulations.class);
        final Fleet mockFleet = mock(Fleet.class);
        final BathymetricGrid mockBathymetricGrid = mock(BathymetricGrid.class);
        final Simulation mockSimulation = mock(Simulation.class);
        final TemporalSchedule mockSchedule = mock(TemporalSchedule.class);

        when(mockSimulation.getTemporalSchedule()).thenReturn(mockSchedule);
        when(mockBathymetricGrid.getField()).thenReturn(new DoubleGrid2D(10, 10, 0));

        final Factory<Regulations> mockRegulationsFactory = mockFactory(mockRegulations);
        final Factory<Fleet> mockFleetFactory = mockFactory(mockFleet);
        final Factory<BathymetricGrid> mockBathymetricGridFactory =
            mockFactory(mockBathymetricGrid);

        final RegulationGridPortrayalFactory factory = new RegulationGridPortrayalFactory(
            mockRegulationsFactory,
            mockFleetFactory,
            mockBathymetricGridFactory
        );

        final FastValueGridPortrayal2D portrayal = factory.newInstance(mockSimulation);

        assertEquals(
            DoubleGrid2D.class, portrayal.getField().getClass(),
            "Expected Portrayal to use a DoubleGrid2D as its field"
        );
    }

    @Test
    void testPortrayalGridInitializedWithBathymetricGridSize() {
        final BathymetricGrid mockBathymetricGrid = mock(BathymetricGrid.class);

        when(mockBathymetricGrid.getField()).thenReturn(new DoubleGrid2D(15, 20, 0));

        final Factory<Regulations> mockRegulationsFactory = mock(Factory.class);
        final Factory<Fleet> mockFleetFactory = mock(Factory.class);
        final Factory<BathymetricGrid> mockBathymetricGridFactory =
            mockFactory(mockBathymetricGrid);

        final Simulation mockSimulation = mock(Simulation.class);
        final TemporalSchedule mockSchedule = mock(TemporalSchedule.class);

        when(mockSimulation.getTemporalSchedule()).thenReturn(mockSchedule);

        final RegulationGridPortrayalFactory factory = new RegulationGridPortrayalFactory(
            mockRegulationsFactory,
            mockFleetFactory,
            mockBathymetricGridFactory
        );

        final FastValueGridPortrayal2D portrayal = factory.newInstance(mockSimulation);

        final DoubleGrid2D gridField = ((DoubleGrid2D) portrayal.getField());
        assertEquals(
            15,
            gridField.getWidth(),
            "Expected the grid's width to match the bathymetric grid"
        );
        assertEquals(
            20,
            gridField.getHeight(),
            "Expected the grid's height to match the bathymetric grid"
        );
    }

    @Test
    void testUpdateGridHandlesForbiddenRegulationsCorrectly() {
        final Regulations mockRegulations = mock(Regulations.class);
        final Fleet mockFleet = mock(Fleet.class);
        final BathymetricGrid mockBathymetricGrid = mock(BathymetricGrid.class);
        final Simulation mockSimulation = mock(Simulation.class);
        final TemporalSchedule mockSchedule = mock(TemporalSchedule.class);

        when(mockSimulation.getTemporalSchedule()).thenReturn(mockSchedule);

        final Int2D mockInt2D = new Int2D(0, 0);

        when(mockBathymetricGrid.getWaterCells()).thenReturn(Stream.of(mockInt2D));
        when(mockFleet.getVessels()).thenReturn(Collections.emptyList());
        when(mockBathymetricGrid.getModelGrid()).thenReturn(mock(ModelGrid.class));

        when(mockRegulations.isForbidden(Mockito.any(DummyFishingAction.class))).thenReturn(true);
        when(mockBathymetricGrid.getField()).thenReturn(new DoubleGrid2D(10, 10, 0));

        final RegulationGridPortrayalFactory.Portrayal portrayal =
            new RegulationGridPortrayalFactory.Portrayal(
                mockSchedule,
                mockRegulations,
                mockFleet,
                mockBathymetricGrid,
                RegulationGridPortrayalFactory.UpdateFrequency.EVERY_MONTH
            );

        portrayal.updateGrid();

        final DoubleGrid2D gridField = portrayal.getGrid();

        assertEquals(
            1,
            gridField.field[mockInt2D.x][mockInt2D.y],
            "Expected the grid cell to be marked forbidden"
        );
    }

    @Test
    void testDrawUpdatesGridOnTemporalChange() {
        final Regulations mockRegulations = mock(Regulations.class);
        final Fleet mockFleet = mock(Fleet.class);
        final BathymetricGrid mockBathymetricGrid = mock(BathymetricGrid.class);
        final Simulation mockSimulation = mock(Simulation.class);
        final TemporalSchedule mockSchedule = mock(TemporalSchedule.class);

        when(mockSimulation.getTemporalSchedule()).thenReturn(mockSchedule);
        when(mockBathymetricGrid.getField()).thenReturn(new DoubleGrid2D(10, 10, 0));
        when(mockRegulations.isForbidden(any())).thenReturn(false);

        final RegulationGridPortrayalFactory.Portrayal portrayal =
            new RegulationGridPortrayalFactory.Portrayal(
                mockSchedule,
                mockRegulations,
                mockFleet,
                mockBathymetricGrid,
                RegulationGridPortrayalFactory.UpdateFrequency.EVERY_MONTH
            );

        when(mockSchedule.getDate().getLong(any())).thenReturn(1L);

        portrayal.draw(null, mock(Graphics2D.class), null);

        assertEquals(
            1L,
            portrayal.getLastUpdated(),
            "Expected lastUpdated to match the current field value"
        );
    }

    private <T> Factory<T> mockFactory(final T instance) {
        final Factory<T> factory = mock(Factory.class);
        when(factory.get(any(Simulation.class))).thenReturn(instance);
        return factory;
    }

}
