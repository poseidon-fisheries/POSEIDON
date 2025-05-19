/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;

public class CompressedExponentialAttractionProbabilityTest {

    @Test
    public void testProbabilityOfAttraction() {

        final Species species = new Species("");

        final CompressedExponentialAttractionProbability f =
            new CompressedExponentialAttractionProbability(
                new double[]{2.0},
                new double[]{1E-3},
                new double[]{1E-5}
            );

        final double delta = 0.001;
        Assertions.assertEquals(0, f.apply(species, 0, 0), delta);
        Assertions.assertEquals(0, f.apply(species, 0, 0), delta);
        Assertions.assertEquals(0.221, f.apply(species, 500, 0), delta);
        Assertions.assertEquals(0.632, f.apply(species, 1000, 0), delta);
        Assertions.assertEquals(0, f.apply(species, 0, 150), delta);
        Assertions.assertEquals(0.790, f.apply(species, 500, 150), delta);
        Assertions.assertEquals(0.998, f.apply(species, 1000, 150), delta);
        Assertions.assertEquals(0, f.apply(species, 0, 300), delta);
        Assertions.assertEquals(0.982, f.apply(species, 500, 300), delta);
        Assertions.assertEquals(1.00, f.apply(species, 1000, 300), delta);
    }
}
