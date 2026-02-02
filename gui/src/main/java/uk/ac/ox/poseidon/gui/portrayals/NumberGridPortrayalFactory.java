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
import lombok.experimental.SuperBuilder;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.Grid2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.simple.ValuePortrayal2D;
import sim.util.Int2D;
import sim.util.gui.ColorMap;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.grids.MutableGrid;
import uk.ac.ox.poseidon.geography.grids.NumberGrid;
import uk.ac.ox.poseidon.gui.palettes.PaletteColorMap;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NumberGridPortrayalFactory
    extends SimulationScopeFactory<FastValueGridPortrayal2D> {

    private String paletteName;
    private String valueName;
    private boolean immutableField;
    private Factory<? super SimulationScope, ? extends NumberGrid<?>> grid;

    @Override
    protected FastValueGridPortrayal2D newInstance(final SimulationScope scope) {
        final var portrayal = new FastValueGridPortrayal2D(valueName, immutableField);
        portrayal.setPortrayalForAll(new ValuePortrayal2D() {
            @Override
            public String getName(final LocationWrapper wrapper) {
                final ValueGridPortrayal2D portrayal =
                    (ValueGridPortrayal2D) wrapper.getFieldPortrayal();
                return portrayal.getValueName() + ": " + wrapper.getObject();
            }
        });
        final NumberGrid<?> numberGrid = grid.get(scope);
        portrayal.setField(toField(numberGrid));
        portrayal.setMap(newColorMap(scope));
        return portrayal;
    }

    protected ColorMap newColorMap(final SimulationScope scope) {
        return new PaletteColorMap(
            paletteName,
            0,
            grid.get(scope).getMaximumValue().doubleValue()
        );
    }

    private static Grid2D toField(final NumberGrid<?> grid) {
        if (grid instanceof MutableGrid<?> mutableGrid) {
            final Grid2D field = ((MutableGrid<?>) mutableGrid).getField();
            if (field instanceof DoubleGrid2D) return field;
        }
        final int width = grid.getModelGrid().getGridWidth();
        final int height = grid.getModelGrid().getGridHeight();
        final DoubleGrid2D copy = new DoubleGrid2D(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                copy.set(x, y, grid.getValue(new Int2D(x, y)).doubleValue());
            }
        }
        return copy;
    }

}
