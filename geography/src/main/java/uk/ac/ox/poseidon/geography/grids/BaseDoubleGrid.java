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

package uk.ac.ox.poseidon.geography.grids;

import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Envelope;

public class BaseDoubleGrid extends AbstractGrid<DoubleGrid2D>
    implements DoubleGrid {

    public BaseDoubleGrid(final ModelGrid modelGrid) {
        this(modelGrid, 0.0);
    }

    public BaseDoubleGrid(
        final ModelGrid modelGrid,
        final double initialValue
    ) {
        this(
            modelGrid,
            new DoubleGrid2D(modelGrid.getGridWidth(), modelGrid.getGridHeight(), initialValue)
        );
    }

    public BaseDoubleGrid(
        final double[][] values
    ) {
        this(new DoubleGrid2D(values));
    }

    public BaseDoubleGrid(final DoubleGrid2D grid2D) {
        this(
            ModelGrid.create(
                grid2D.width,
                grid2D.height,
                new Envelope(
                    0, grid2D.width,
                    0, grid2D.height
                )
            ),
            grid2D
        );
    }

    public BaseDoubleGrid(
        final ModelGrid modelGrid,
        final double[][] values
    ) {
        this(modelGrid, new DoubleGrid2D(values));
    }

    public BaseDoubleGrid(
        final ModelGrid modelGrid,
        final DoubleGrid2D grid
    ) {
        super(modelGrid, new DoubleGrid2D(grid));
    }

    @Override
    public double getMinimumValue() {
        return field.min();
    }

    @Override
    public double getMaximumValue() {
        return field.max();
    }

    @Override
    public double getValue(
        final Int2D cell
    ) {
        return field.get(cell.x, cell.y);
    }

    @Override
    public double getSum() {
        double sum = 0.0;
        final int width = field.width;
        final int height = field.height;
        final double[][] a = field.field;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                sum += a[x][y];
            }
        }
        return sum;
    }
}
