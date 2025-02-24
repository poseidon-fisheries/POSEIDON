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

import lombok.Getter;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.Arrays;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

public class DefaultBathymetricGrid extends DoubleGrid implements BathymetricGrid {

    @Getter(value = PRIVATE, lazy = true)
    private final Int2D[] landCellsArray =
        BathymetricGrid.super.getLandCells().toArray(Int2D[]::new);

    @Getter(value = PRIVATE, lazy = true)
    private final Int2D[] waterCellsArray =
        BathymetricGrid.super.getWaterCells().toArray(Int2D[]::new);

    @Getter(value = PRIVATE, lazy = true)
    private final Int2D[] activeWaterCellsArray =
        BathymetricGrid.super.getActiveWaterCells().toArray(Int2D[]::new);

    public DefaultBathymetricGrid(final ModelGrid modelGrid) {
        super(modelGrid);
    }

    public DefaultBathymetricGrid(final double[][] values) {
        super(values);
    }

    DefaultBathymetricGrid(
        final ModelGrid modelGrid,
        final double initialValue
    ) {
        super(modelGrid, initialValue);
    }

    public DefaultBathymetricGrid(
        final ModelGrid modelGrid,
        final double[][] values
    ) {
        super(modelGrid, values);
    }

    DefaultBathymetricGrid(
        final ModelGrid modelGrid,
        final DoubleGrid2D grid
    ) {
        super(modelGrid, grid);
    }

    @Override
    public Stream<Int2D> getActiveWaterCells() {
        return Arrays.stream(getActiveWaterCellsArray());
    }

    @Override
    public Stream<Int2D> getWaterCells() {
        return Arrays.stream(getWaterCellsArray());
    }

    @Override
    public Stream<Int2D> getLandCells() {
        return Arrays.stream(getLandCellsArray());
    }

    @Override
    public double getElevation(final Int2D cell) {
        return getDouble(cell);
    }

}
