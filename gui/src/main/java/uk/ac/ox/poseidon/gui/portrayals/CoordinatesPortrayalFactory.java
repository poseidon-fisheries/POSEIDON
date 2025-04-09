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

import lombok.RequiredArgsConstructor;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.text.DecimalFormat;

@RequiredArgsConstructor
public class CoordinatesPortrayalFactory extends SimulationScopeFactory<ObjectGridPortrayal2D> {

    private final Factory<? extends ModelGrid> modelGrid;
    private final DecimalFormat decimalFormat;

    public CoordinatesPortrayalFactory(
        final Factory<? extends ModelGrid> modelGrid,
        final int decimalPlaces
    ) {
        this.modelGrid = modelGrid;
        this.decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(decimalPlaces);
        decimalFormat.setMinimumFractionDigits(decimalPlaces);
    }

    @Override
    protected ObjectGridPortrayal2D newInstance(final Simulation simulation) {
        final ObjectGridPortrayal2D objectGridPortrayal2D = new ObjectGridPortrayal2D();
        objectGridPortrayal2D.setField(modelGrid.get(simulation).getCoordinatesGrid());
        objectGridPortrayal2D.setPortrayalForAll(
            new SimplePortrayal2D() {
                @Override
                public boolean hitObject(
                    final Object object,
                    final DrawInfo2D range
                ) {
                    return true;
                }

                @Override
                public String getName(final LocationWrapper wrapper) {
                    final Coordinate coordinate = (Coordinate) wrapper.getObject();
                    return "Coordinates: %.3f, %.3f".formatted(coordinate.lon, coordinate.lat);
                }
            }
        );
        return objectGridPortrayal2D;
    }
}
