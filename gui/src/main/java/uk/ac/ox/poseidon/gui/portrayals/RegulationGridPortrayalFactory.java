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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.Int2D;
import sim.util.gui.SimpleColorMap;
import uk.ac.ox.poseidon.agents.behaviours.fishing.DummyFishingAction;
import uk.ac.ox.poseidon.agents.fleets.Fleet;
import uk.ac.ox.poseidon.agents.regulations.Regulations;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegulationGridPortrayalFactory extends SimulationScopeFactory<FastValueGridPortrayal2D> {
    private static final int OPACITY = 32;

    private Factory<? extends Regulations> regulations;
    private Factory<? extends Fleet> fleet;
    private Factory<? extends BathymetricGrid> bathymetric;

    @Override
    protected FastValueGridPortrayal2D newInstance(final Simulation simulation) {
        return new Portrayal(
            simulation.getTemporalSchedule(),
            regulations.get(simulation),
            fleet.get(simulation),
            bathymetric.get(simulation)
        );
    }

    private static class Portrayal extends FastValueGridPortrayal2D {

        private final TemporalSchedule schedule;
        private final Regulations regulations;
        private final Fleet fleet;
        private final BathymetricGrid bathymetricGrid;
        private final DoubleGrid2D grid;

        private LocalDate lastUpdated;

        private Portrayal(
            final TemporalSchedule schedule,
            final Regulations regulations,
            final Fleet fleet,
            final BathymetricGrid bathymetricGrid
        ) {
            super();
            this.regulations = regulations;
            this.fleet = fleet;
            this.bathymetricGrid = bathymetricGrid;
            this.grid = new DoubleGrid2D(
                bathymetricGrid.getField().width,
                bathymetricGrid.getField().height,
                0
            );
            this.schedule = schedule;
            setField(grid);
            setMap(new SimpleColorMap(
                0,
                1,
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, OPACITY)
            ));
        }

        @Override
        public void draw(
            final Object object,
            final Graphics2D graphics,
            final DrawInfo2D info
        ) {
            // TODO: allow more flexible update frequency than "once a day"
            final LocalDate currentDate = schedule.getDate();
            if (!currentDate.equals(lastUpdated)) {
                updateGrid();
                lastUpdated = currentDate;
            }
            super.draw(object, graphics, info);
        }

        private void updateGrid() {
            for (final Int2D cell : bathymetricGrid.getWaterCells()) {
                final LocalDateTime dateTime = schedule.getDateTime();
                final boolean forbidden =
                    fleet.getVessels()
                        .stream()
                        .map(vessel ->
                            new DummyFishingAction(
                                dateTime,
                                vessel,
                                bathymetricGrid.getGridExtent().toCoordinate(cell)
                            )
                        ).anyMatch(regulations::isForbidden);
                grid.field[cell.x][cell.y] = forbidden ? 1 : 0;
            }
        }
    }

}
