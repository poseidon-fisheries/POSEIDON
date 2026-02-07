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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.quantities.KilogramsFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.allocators.SupplierAllocatorFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;
import uk.ac.ox.poseidon.geography.grids.DoubleGridFromAllocatorFactory;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.NormalisedDoubleGridFromAllocatorFactory;
import uk.ac.ox.poseidon.geography.predicates.ActiveWaterCellPredicateFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

import static uk.ac.ox.poseidon.core.suppliers.SupplierFactories.constantDouble;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CarryingCapacityGridFactory<S extends Scope>
    extends RelativeScopeFactory<S, CarryingCapacityGrid> {

    private Factory<? super S, ? extends DoubleGrid> grid;

    @Override
    protected CarryingCapacityGrid newInstance(final S scope) {
        return new CarryingCapacityGrid(grid.get(scope));
    }

    public static <S extends Scope> CarryingCapacityGridFactory<S> ofUniformCapacity(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super S, ? extends Quantity<Mass>> carryingCapacity
    ) {
        return new CarryingCapacityGridFactory<>(
            new DoubleGridFromAllocatorFactory<>(
                modelGrid,
                new SupplierAllocatorFactory<>(
                    constantDouble(new KilogramsFactory<>(carryingCapacity))
                ),
                new ActiveWaterCellPredicateFactory<>(bathymetricGrid)
            )
        );
    }

    public static <S extends Scope> CarryingCapacityGridFactory<S> ofTotalCapacity(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super S, ? extends Quantity<Mass>> totalCarryingCapacity
    ) {
        return new CarryingCapacityGridFactory<>(
            new NormalisedDoubleGridFromAllocatorFactory<>(
                modelGrid,
                new SupplierAllocatorFactory<>(constantDouble(1.0)),
                new ActiveWaterCellPredicateFactory<>(bathymetricGrid),
                new KilogramsFactory<>(totalCarryingCapacity)
            )
        );
    }

}
