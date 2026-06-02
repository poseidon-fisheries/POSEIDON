/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.allocators.Allocator;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BiomassGridsFactory extends SimulationScopeFactory<List<BiomassGrid>> {

    private Factory<? super SimulationScope, ? extends ModelGrid> modelGrid;
    private Factory<? super SimulationScope, ? extends List<? extends Species>> species;
    private Factory<? super SimulationScope, ? extends List<? extends Allocator>> biomassAllocators;

    @Override
    protected List<BiomassGrid> newInstance(final SimulationScope scope) {

        final ModelGrid modelGrid = this.modelGrid.get(scope);
        final List<? extends Species> species = this.species.get(scope);
        final List<? extends Allocator> biomassAllocators = this.biomassAllocators.get(scope);

        final int numAllocators = biomassAllocators.size();
        if (numAllocators != 1) {
            checkState(
                numAllocators == species.size(),
                "Number of biomass allocators must either be 1 " +
                    "or %s (the same size as species) but was %s.",
                species.size(),
                numAllocators
            );
        }

        return IntStream
            .range(0, species.size())
            .mapToObj(i -> {
                final Allocator biomassAllocator =
                    biomassAllocators.get(numAllocators == 1 ? 0 : i);
                final double[][] biomassArray = modelGrid.makeDoubleArray();
                modelGrid.getAllCells().forEach(cell ->
                    biomassArray[cell.x][cell.y] = biomassAllocator.applyAsDouble(cell)
                );
                return ((BiomassGrid) new DefaultBiomassGrid(
                    modelGrid,
                    species.get(i),
                    biomassArray
                ));
            })
            .toList();
    }

}
