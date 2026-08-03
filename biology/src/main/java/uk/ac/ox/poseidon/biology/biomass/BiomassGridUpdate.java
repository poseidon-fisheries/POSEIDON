/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

import lombok.Value;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

import java.io.Serial;
import java.util.List;

/**
 * Fully replaces the contents of each species' live {@link BiomassGrid} in {@code target} with
 * the corresponding values from {@code snapshot}, cell by cell over the target's active cells.
 */
@Value
public class BiomassGridUpdate implements Steppable {

    @Serial private static final long serialVersionUID = 5460918327904618374L;

    List<? extends SpeciesGrid> snapshot;
    FisheableBiomassGrids target;

    @Override
    public void step(final SimState simState) {
        for (final SpeciesGrid speciesGrid : snapshot) {
            final BiomassGrid targetGrid = target.get(speciesGrid.getSpecies());
            for (final Int2D cell : targetGrid.getModelGrid().getActiveCells()) {
                targetGrid.setBiomass(cell, speciesGrid.getValue(cell));
            }
        }
    }
}
