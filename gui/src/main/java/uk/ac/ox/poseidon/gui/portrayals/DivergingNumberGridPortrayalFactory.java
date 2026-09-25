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

package uk.ac.ox.poseidon.gui.portrayals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.util.gui.ColorMap;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;
import uk.ac.ox.poseidon.gui.palettes.PaletteColorMap;

import java.util.DoubleSummaryStatistics;

import static java.lang.Math.abs;
import static java.lang.Math.max;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * A {@link NumberGridPortrayalFactory} ranged symmetrically around zero (from
 * {@code -max(|min|, |max|)} to {@code +max(|min|, |max|)}), for grids with both positive and
 * negative values (e.g. elevation).
 */
public class DivergingNumberGridPortrayalFactory extends NumberGridPortrayalFactory {

    /**
     * @param paletteName    the colour palette to use
     * @param valueName      the label shown for cell values
     * @param immutableField whether the underlying grid data is immutable between draws
     * @param grid           the grid to portray
     */
    public DivergingNumberGridPortrayalFactory(
        final String paletteName,
        final String valueName,
        final boolean immutableField,
        final Factory<? super SimulationScope, ? extends DoubleGrid> grid
    ) {
        super(paletteName, valueName, immutableField, grid);
    }

    /** @return a {@link PaletteColorMap} ranged symmetrically around zero, spanning the grid's actual extremes */
    @Override
    protected ColorMap newColorMap(final SimulationScope scope) {
        final DoubleGrid grid = getGrid().get(scope);
        final DoubleSummaryStatistics stats =
            grid
                .getModelGrid()
                .getAllCells()
                .mapToDouble(grid::getValue)
                .summaryStatistics();
        final double absMax = max(abs(stats.getMin()), abs(stats.getMax()));
        return new PaletteColorMap(getPaletteName(), -absMax, absMax);
    }

}
