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

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.MutableGrid;

import static org.assertj.core.api.Assertions.assertThat;

class ImmutableBiomassGridTest {

    private static final Species COD = new Species("COD", null, "Cod");

    @Test
    void isNotMutableOrFisheable() {
        assertThat(MutableGrid.class.isAssignableFrom(ImmutableBiomassGrid.class)).isFalse();
        assertThat(BiomassGrid.class.isAssignableFrom(ImmutableBiomassGrid.class)).isFalse();
    }

    @Test
    void doesNotAliasTheSourceArray() {
        final ModelGrid modelGrid = ModelGrid.create(2, 1, new Envelope(0, 2, 0, 1));
        final double[][] values = {{1.0}, {2.0}};

        final ImmutableBiomassGrid grid = new ImmutableBiomassGrid(modelGrid, COD, values);
        values[0][0] = 999.0;

        assertThat(grid.getValue(new Int2D(0, 0))).isEqualTo(1.0);
        assertThat(grid.getSpecies()).isEqualTo(COD);
    }

}
