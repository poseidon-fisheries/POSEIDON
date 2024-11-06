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

package uk.ac.ox.poseidon.gui.portrayals;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.util.gui.ColorMap;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.NumberGrid;
import uk.ac.ox.poseidon.gui.palettes.PaletteColorMap;

import java.util.DoubleSummaryStatistics;

import static java.lang.Math.abs;
import static java.lang.Math.max;

@Getter
@Setter
@NoArgsConstructor
public class DivergingNumberGridPortrayalFactory extends NumberGridPortrayalFactory {

    public DivergingNumberGridPortrayalFactory(
        final String paletteName,
        final String valueName,
        final boolean immutableField,
        final Factory<? extends NumberGrid<?, ?>> grid
    ) {
        super(paletteName, valueName, immutableField, grid);
    }

    @Override
    protected ColorMap newColorMap(final Simulation simulation) {
        final NumberGrid<?, ?> grid = getGrid().get(simulation);
        final DoubleSummaryStatistics stats =
            grid
                .getGridExtent()
                .getAllCells()
                .stream()
                .mapToDouble(cell -> grid.getValue(cell).doubleValue())
                .summaryStatistics();
        final double absMax = max(abs(stats.getMin()), abs(stats.getMax()));
        return new PaletteColorMap(getPaletteName(), -absMax, absMax);
    }

}
