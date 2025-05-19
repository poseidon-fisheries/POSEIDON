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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.util.gui.ColorMap;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.NumberGrid;
import uk.ac.ox.poseidon.gui.palettes.PaletteColorMap;

@Getter
@Setter
@NoArgsConstructor
public class NumberGridWithCapacityPortrayalFactory extends NumberGridPortrayalFactory {

    private Factory<? extends NumberGrid<?, ?>> capacityGrid;

    public NumberGridWithCapacityPortrayalFactory(
        final String paletteName,
        final String valueName,
        final boolean immutableField,
        final Factory<? extends NumberGrid<?, ?>> grid,
        final Factory<? extends NumberGrid<?, ?>> capacityGrid
    ) {
        super(paletteName, valueName, immutableField, grid);
        this.capacityGrid = capacityGrid;
    }

    @Override
    protected ColorMap newColorMap(final Simulation simulation) {
        return new PaletteColorMap(
            getPaletteName(),
            0,
            this.capacityGrid.get(simulation).getMaximumValue().doubleValue()
        );
    }
}
