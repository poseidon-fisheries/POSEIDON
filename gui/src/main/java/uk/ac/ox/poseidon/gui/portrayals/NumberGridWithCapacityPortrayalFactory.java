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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.util.gui.ColorMap;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;
import uk.ac.ox.poseidon.gui.palettes.PaletteColorMap;

/**
 * A {@link NumberGridPortrayalFactory} ranged {@code [0, capacityGrid's max value]} instead of
 * the portrayed grid's own maximum — for grids (e.g. biomass) meant to be read against a fixed
 * capacity rather than their current extent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NumberGridWithCapacityPortrayalFactory extends NumberGridPortrayalFactory {

    private Factory<? super SimulationScope, ? extends DoubleGrid> capacityGrid;

    /**
     * @param paletteName    the colour palette to use
     * @param valueName      the label shown for cell values
     * @param immutableField whether the underlying grid data is immutable between draws
     * @param grid           the grid to portray
     * @param capacityGrid   the grid whose maximum value bounds the colour range
     */
    public NumberGridWithCapacityPortrayalFactory(
        final String paletteName,
        final String valueName,
        final boolean immutableField,
        final Factory<? super SimulationScope, ? extends DoubleGrid> grid,
        final Factory<? super SimulationScope, ? extends DoubleGrid> capacityGrid
    ) {
        super(paletteName, valueName, immutableField, grid);
        this.capacityGrid = capacityGrid;
    }

    /** @return a {@link PaletteColorMap} ranged {@code [0, capacityGrid's max value]} */
    @Override
    protected ColorMap newColorMap(final SimulationScope scope) {
        return new PaletteColorMap(
            getPaletteName(),
            0,
            this.capacityGrid.get(scope).getMaximumValue()
        );
    }
}
