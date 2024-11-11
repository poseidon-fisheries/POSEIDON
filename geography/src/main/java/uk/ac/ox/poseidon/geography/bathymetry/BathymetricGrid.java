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

package uk.ac.ox.poseidon.geography.bathymetry;

import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.grids.NumberGrid;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public interface BathymetricGrid extends NumberGrid<Double, DoubleGrid2D> {

    default List<Int2D> getWaterCells() {
        return getGridExtent()
            .getAllCells()
            .stream()
            .filter(this::isWater)
            .collect(toImmutableList());
    }

    default List<Int2D> getLandCells() {
        return getGridExtent()
            .getAllCells()
            .stream()
            .filter(this::isLand)
            .collect(toImmutableList());
    }

    default List<Int2D> getAllCells() {
        return getGridExtent().getAllCells();
    }

    default boolean isLand(final Int2D cell) {
        return !isWater(cell);
    }

    default boolean isWater(final Int2D cell) {
        return getElevation(cell) < 0;
    }

    double getElevation(final Int2D cell);

}
