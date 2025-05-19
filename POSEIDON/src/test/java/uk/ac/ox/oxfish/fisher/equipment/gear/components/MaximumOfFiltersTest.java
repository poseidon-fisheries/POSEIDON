/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;

public class MaximumOfFiltersTest {


    @Test
    public void maximumWorksWell() {

        Species species = new Species(
            "test",
            new FromListMeristics(
                new double[]{10, 20, 30}, 2
            )
        );
        ArrayFilter firstFilter = new ArrayFilter(false,
            new double[]{0.5, 0, 0.5}, new double[]{0.2, 0, 0.2}
        );

        ArrayFilter secondFilter = new ArrayFilter(false,
            new double[]{0, 0.5, 0}, new double[]{1, 1, 1}
        );

        MaximumOfFilters filters = new MaximumOfFilters(
            firstFilter, secondFilter
        );

        double[][] abundance = new double[2][3];
        for (int subdivision = 0; subdivision < 2; subdivision++) {
            for (int bin = 0; bin < 3; bin++) {
                abundance[subdivision][bin] = 1;
            }
        }

        final double[][] filtered = filters.filter(species, abundance);
        Assertions.assertArrayEquals(filtered[0], new double[]{0.5, 0.5, 0.5}, .001);
        Assertions.assertArrayEquals(filtered[1], new double[]{1, 1, 1}, .001);

    }
}
