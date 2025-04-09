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

import lombok.*;
import sim.portrayal.LocationWrapper;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.simple.ValuePortrayal2D;
import sim.util.gui.ColorMap;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.grids.NumberGrid;
import uk.ac.ox.poseidon.gui.palettes.PaletteColorMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NumberGridPortrayalFactory
    extends SimulationScopeFactory<FastValueGridPortrayal2D> {

    private String paletteName;
    private String valueName;
    private boolean immutableField;
    private Factory<? extends NumberGrid<?, ?>> grid;

    @Override
    protected FastValueGridPortrayal2D newInstance(final @NonNull Simulation simulation) {
        final var portrayal = new FastValueGridPortrayal2D(valueName, immutableField);
        portrayal.setPortrayalForAll(new ValuePortrayal2D() {
            @Override
            public String getName(final LocationWrapper wrapper) {
                final ValueGridPortrayal2D portrayal =
                    (ValueGridPortrayal2D) wrapper.getFieldPortrayal();
                return portrayal.getValueName() + ": " + wrapper.getObject();
            }
        });
        portrayal.setField(grid.get(simulation).getField());
        portrayal.setMap(newColorMap(simulation));
        return portrayal;
    }

    protected ColorMap newColorMap(final Simulation simulation) {
        return new PaletteColorMap(
            paletteName,
            0,
            grid.get(simulation).getMaximumValue().doubleValue()
        );
    }

}
