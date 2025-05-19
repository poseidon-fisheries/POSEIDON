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

public class SimplifiedDoubleNormalFilterTest {


    @Test
    public void doubleNormal() {

        final double[] length = new double[126];
        for (int i = 0; i <= 125; i++)
            length[i] = i;

        Species species = new Species(
            "test",
            new FromListMeristics(
                new double[126],
                length,
                1
            )
        );

        SimplifiedDoubleNormalFilter filter = new SimplifiedDoubleNormalFilter(true, false,
            30, 5, 10
        );

        final double[][] selex = filter.computeSelectivity(species);

        Assertions.assertEquals(selex[0][30], 1.0, .001);
        Assertions.assertEquals(selex[0][20], 6.250000e-02, .001);
        Assertions.assertEquals(selex[0][40], 0.5, .001);
        Assertions.assertEquals(selex[0][50], 6.250000e-02, .001);

    }
}
