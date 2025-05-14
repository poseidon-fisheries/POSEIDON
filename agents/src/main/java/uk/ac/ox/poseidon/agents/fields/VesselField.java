/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.agents.fields;

import lombok.Getter;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

@Getter
public class VesselField {

    private final Continuous2D field;
    private final ModelGrid modelGrid;

    VesselField(final ModelGrid modelGrid) {
        this.field = new Continuous2D(
            1,
            modelGrid.getGridWidth(),
            modelGrid.getGridHeight()
        );
        this.modelGrid = modelGrid;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setCell(
        final Vessel vessel,
        final Int2D cell
    ) {
        return setPoint(vessel, modelGrid.toPoint(cell));
    }

    @SuppressWarnings("WeakerAccess")
    public boolean setPoint(
        final Vessel vessel,
        final Double2D location
    ) {
        return field.setObjectLocation(vessel, location);
    }

    public Double2D getPoint(final Vessel vessel) {
        return field.getObjectLocation(vessel);
    }

    public Int2D getCell(final Vessel vessel) {
        return modelGrid.toCell(getPoint(vessel));
    }

}
