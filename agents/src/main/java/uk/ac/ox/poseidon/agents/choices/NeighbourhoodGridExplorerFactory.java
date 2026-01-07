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

package uk.ac.ox.poseidon.agents.choices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NeighbourhoodGridExplorerFactory extends VesselScopeFactory<NeighbourhoodCellPicker> {

    private Factory<? super VesselScope, ? extends OptionValues<Int2D>> optionValues;
    private Factory<? super VesselScope, ? extends Predicate<Int2D>> cellPredicate;
    private Factory<? super VesselScope, ? extends GridPathFinder> pathFinder;
    private Factory<? super VesselScope, ? extends IntSupplier> neighbourhoodSizeSupplier;

    @Override
    protected NeighbourhoodCellPicker newInstance(final VesselScope scope) {
        return new NeighbourhoodCellPicker(
            scope.getVessel(),
            optionValues.get(scope),
            cellPredicate.get(scope),
            pathFinder.get(scope),
            neighbourhoodSizeSupplier.get(scope),
            scope.getSimulation().random
        );
    }
}
