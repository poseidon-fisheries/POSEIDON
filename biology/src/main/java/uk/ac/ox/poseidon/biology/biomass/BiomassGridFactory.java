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

import lombok.*;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BiomassGridFactory extends SimulationScopeFactory<BiomassGrid> {

    @NonNull private Factory<? extends GridExtent> gridExtent;
    @NonNull private Factory<? extends Species> species;
    @NonNull private Factory<? extends BiomassAllocator> biomassAllocator;

    @Override
    protected BiomassGrid newInstance(final Simulation simulation) {
        final BiomassAllocator biomassAllocator = this.biomassAllocator.get(simulation);
        final GridExtent gridExtent = this.gridExtent.get(simulation);
        final double[][] biomassArray = gridExtent.makeDoubleArray();
        gridExtent.getAllCells().forEach(cell ->
            biomassArray[cell.x][cell.y] = biomassAllocator.applyAsDouble(cell)
        );
        return new DefaultBiomassGrid(gridExtent, species.get(simulation), biomassArray);
    }

}