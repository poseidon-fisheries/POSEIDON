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

package uk.ac.ox.poseidon.biology.biomass;

import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.FisheableGrid;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.grids.NumberGrid;

public interface BiomassGrid
    extends NumberGrid<Double, DoubleGrid2D>, FisheableGrid<Biomass> {

    Species getSpecies();

    default Biomass getBiomass(final Int2D cell) {
        return Biomass.of(getDouble(cell));
    }

    double getDouble(final Int2D cell);

    default void setBiomass(
        final Int2D cell,
        final Biomass biomass
    ) {
        setBiomass(cell, biomass.getValue());
    }

    void setBiomass(
        final Int2D cell,
        final double value
    );
}