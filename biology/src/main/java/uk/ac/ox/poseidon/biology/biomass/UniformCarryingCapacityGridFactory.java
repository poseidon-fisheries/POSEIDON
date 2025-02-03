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
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

import static si.uom.NonSI.TONNE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UniformCarryingCapacityGridFactory
    extends SimulationScopeFactory<CarryingCapacityGrid> {

    @NonNull Factory<? extends BathymetricGrid> bathymetricGrid;
    private Factory<? extends Quantity<Mass>> carryingCapacity;

    @Override
    protected CarryingCapacityGrid newInstance(final Simulation simulation) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(simulation);
        final GridExtent gridExtent = bathymetricGrid.getGridExtent();
        final double[][] array = gridExtent.makeDoubleArray();
        final double carryingCapacityInTonnes =
            carryingCapacity
                .get(simulation)
                .to(TONNE)
                .getValue()
                .doubleValue();
        bathymetricGrid.getAllCells().forEach(cell ->
            array[cell.x][cell.y] =
                bathymetricGrid.isWater(cell)
                    ? carryingCapacityInTonnes
                    : Double.NaN
        );
        return new CarryingCapacityGrid(gridExtent, array);
    }
}
