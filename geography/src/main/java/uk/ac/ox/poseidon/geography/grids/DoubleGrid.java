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

package uk.ac.ox.poseidon.geography.grids;

import com.vividsolutions.jts.geom.Envelope;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;

public class DoubleGrid extends AbstractGrid<DoubleGrid2D>
    implements NumberGrid<Double, DoubleGrid2D> {

    protected final DoubleGrid2D doubleGrid2D;

    public DoubleGrid(final GridExtent gridExtent) {
        this(gridExtent, 0.0);
    }

    public DoubleGrid(
        final GridExtent gridExtent,
        final double initialValue
    ) {
        this(
            gridExtent,
            new DoubleGrid2D(gridExtent.getGridWidth(), gridExtent.getGridHeight(), initialValue)
        );
    }

    public DoubleGrid(
        final double[][] values
    ) {
        this(new DoubleGrid2D(values));
    }

    public DoubleGrid(final DoubleGrid2D grid2D) {
        this(
            new GridExtent(
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

    public DoubleGrid(
        final GridExtent gridExtent,
        final double[][] values
    ) {
        this(gridExtent, new DoubleGrid2D(values));
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public DoubleGrid(
        final GridExtent gridExtent,
        final DoubleGrid2D grid
    ) {
        super(gridExtent, grid);
        this.doubleGrid2D = grid;
    }

    @Override
    public Double getValue(final Int2D cell) {
        return getDouble(cell);
    }

    @Override
    public Double getMinimumValue() {
        return doubleGrid2D.min();
    }

    @Override
    public Double getMaximumValue() {
        return doubleGrid2D.max();
    }

    public double getDouble(
        final Int2D cell
    ) {
        return this.doubleGrid2D.get(cell.x, cell.y);
    }

}
