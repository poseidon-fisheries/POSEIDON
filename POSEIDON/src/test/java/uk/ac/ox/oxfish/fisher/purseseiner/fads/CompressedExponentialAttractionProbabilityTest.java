/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import junit.framework.TestCase;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;

public class CompressedExponentialAttractionProbabilityTest extends TestCase {

    public void testProbabilityOfAttraction() {

        final Species species = new Species("");

        final CompressedExponentialAttractionProbability<BiomassLocalBiology, BiomassFad> f =
            new CompressedExponentialAttractionProbability<>(
                new double[] {2.0},
                new double[] {1E-3},
                new double[] {1E-5}
            );

        final double delta = 0.001;
        assertEquals(0, f.apply(species, 0, 0), delta);
        assertEquals(0, f.apply(species, 0, 0), delta);
        assertEquals(0.221, f.apply(species, 500, 0), delta);
        assertEquals(0.632, f.apply(species, 1000, 0), delta);
        assertEquals(0, f.apply(species, 0, 150), delta);
        assertEquals(0.790, f.apply(species, 500, 150), delta);
        assertEquals(0.998, f.apply(species, 1000, 150), delta);
        assertEquals(0, f.apply(species, 0, 300), delta);
        assertEquals(0.982, f.apply(species, 500, 300), delta);
        assertEquals(1.00, f.apply(species, 1000, 300), delta);
    }
}