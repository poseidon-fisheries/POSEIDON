/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

import lombok.Data;
import lombok.NonNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

import java.io.Serial;
import java.util.List;

/**
 * A biomass grower that operates on the total biomass of the grid.
 */
@Data
public class CommonBiomassGrower implements Steppable {

    @Serial private static final long serialVersionUID = -7273150961650782548L;
    @NonNull private final BiomassGrid biomassGrid;
    @NonNull private final CarryingCapacityGrid carryingCapacityGrid;
    @NonNull private final BiomassGrowthRule biomassGrowthRule;

    @Override
    public void step(final SimState simState) {

        final List<Int2D> habitableCells = carryingCapacityGrid.getHabitableCells();
        double currentBiomass = 0;
        double totalCarryingCapacity = 0;
        for (final Int2D cell : habitableCells) {
            currentBiomass += biomassGrid.getValue(cell);
            totalCarryingCapacity += carryingCapacityGrid.getCarryingCapacity(cell);
        }

        final double newTotalBiomass =
            biomassGrowthRule.newBiomass(currentBiomass, totalCarryingCapacity);

        final double recruitment = newTotalBiomass - currentBiomass;

    }
}
