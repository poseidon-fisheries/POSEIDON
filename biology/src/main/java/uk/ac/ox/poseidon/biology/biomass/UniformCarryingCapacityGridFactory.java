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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.GRAM;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UniformCarryingCapacityGridFactory
    extends SimulationScopeFactory<CarryingCapacityGrid> {

    private Factory<? super SimulationScope, ? extends BathymetricGrid> bathymetricGrid;
    private Factory<? super SimulationScope, ? extends Quantity<Mass>> carryingCapacity;

    @Override
    protected CarryingCapacityGrid newInstance(final SimulationScope scope) {
        final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(scope);
        final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
        final double[][] array = modelGrid.makeDoubleArray();
        final double carryingCapacityInKg =
            carryingCapacity
                .get(scope)
                .to(KILO(GRAM))
                .getValue()
                .doubleValue();
        bathymetricGrid.getAllCells().forEach(cell ->
            array[cell.x][cell.y] =
                modelGrid.isActive(cell) && bathymetricGrid.isWater(cell)
                    ? carryingCapacityInKg
                    : Double.NaN
        );
        return new CarryingCapacityGrid(modelGrid, array);
    }
}
