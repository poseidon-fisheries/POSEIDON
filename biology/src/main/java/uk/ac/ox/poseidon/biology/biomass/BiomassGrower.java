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

package uk.ac.ox.poseidon.biology.biomass;

import lombok.Data;
import lombok.NonNull;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.Serial;

@Data
public class BiomassGrower implements Steppable {

    @Serial private static final long serialVersionUID = -7273150961650782548L;
    @NonNull private final BiomassGrid biomassGrid;
    @NonNull private final CarryingCapacityGrid carryingCapacityGrid;
    @NonNull private final BiomassGrowthRule biomassGrowthRule;

    @Override
    public void step(final SimState simState) {
        carryingCapacityGrid.getHabitableCells().forEach(location ->
            biomassGrid.setBiomass(
                location,
                biomassGrowthRule.newBiomass(
                    biomassGrid.getDouble(location),
                    carryingCapacityGrid.getCarryingCapacity(location)
                )
            )
        );
    }
}