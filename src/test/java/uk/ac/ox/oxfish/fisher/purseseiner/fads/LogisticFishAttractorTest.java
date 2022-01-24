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

import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import uk.ac.ox.oxfish.biology.Species;

public class LogisticFishAttractorTest extends TestCase {

    public void testProbabilityOfAttraction() {

        final Species species = mock(Species.class);
        final LogisticFishBiomassAttractor attractor =
            new LogisticFishBiomassAttractor(
                null,
                ImmutableMap.of(species, 2.0),
                ImmutableMap.of(species, 1E-3),
                ImmutableMap.of(species, 1E-5),
                ImmutableMap.of(species, 0.0)
            );

        final double delta = 0.001;
        assertEquals(0, attractor.probabilityOfAttraction(species, 0, 0), delta);
        assertEquals(0, attractor.probabilityOfAttraction(species, 0, 0), delta);
        assertEquals(0.221, attractor.probabilityOfAttraction(species, 500, 0), delta);
        assertEquals(0.632, attractor.probabilityOfAttraction(species, 1000, 0), delta);
        assertEquals(0, attractor.probabilityOfAttraction(species, 0, 150), delta);
        assertEquals(0.790, attractor.probabilityOfAttraction(species, 500, 150), delta);
        assertEquals(0.998, attractor.probabilityOfAttraction(species, 1000, 150), delta);
        assertEquals(0, attractor.probabilityOfAttraction(species, 0, 300), delta);
        assertEquals(0.982, attractor.probabilityOfAttraction(species, 500, 300), delta);
        assertEquals(1.00, attractor.probabilityOfAttraction(species, 1000, 300), delta);
    }
}