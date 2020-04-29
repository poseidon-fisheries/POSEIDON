/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.equipment.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.fillBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.makeBiology;

public class FadTest {

    private final GlobalBiology globalBiology = new GlobalBiology(new Species("A"), new Species("B"));

    @Test
    public void releaseFish() {

        // Make a full FAD, with a carrying capacity of 0.75...
        final BiomassLocalBiology fadBiology = makeBiology(globalBiology, 0.75);
        fillBiology(fadBiology);
        final FadManager fadManager = mock(FadManager.class, RETURNS_DEEP_STUBS);
        final Fad fad = new Fad(fadManager, fadBiology, ImmutableMap.of(), 0.5, 0);

        // ...and an empty tile biology, with a carrying capacity of 1.0:
        VariableBiomassBasedBiology tileBiology = makeBiology(globalBiology, 1.0);

        // generate failed attempt to release the FAD's fish into the tile biology
        final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
        when(rng.nextDouble()).thenReturn(1.0);
        fad.maybeReleaseFish(globalBiology.getSpecies(), tileBiology, rng);
        // check that the FAD is still full and the tile biology is still empty
        assertTrue(fad.getBiology().isFull());
        assertTrue(tileBiology.isEmpty());

        // release the FAD's fish into the tile biology
        when(rng.nextDouble()).thenReturn(0.0);
        fad.maybeReleaseFish(globalBiology.getSpecies(), tileBiology, rng);
        // Check that the FAD is now empty and the tile has received the fish
        assertTrue(fadBiology.isEmpty());
        for (Species species : globalBiology.getSpecies())
            assertEquals(tileBiology.getBiomass(species), fadBiology.getCarryingCapacity(species), 0d);

        // Refill the FAD and release another batch of FAD fish into the tile biology
        fillBiology(fadBiology);
        fad.maybeReleaseFish(globalBiology.getSpecies(), tileBiology, rng);

        // Check that the FAD is now empty and the tile is now at full carrying capacity
        assertTrue(fadBiology.isEmpty());
        assertTrue(tileBiology.isFull());

        // Fill the FAD one last time and release the fish to nowhere
        fillBiology(fadBiology);
        when(rng.nextDouble()).thenReturn(1.0);
        fad.maybeReleaseFish(globalBiology.getSpecies(), rng);
        assertFalse(fadBiology.isEmpty());
        when(rng.nextDouble()).thenReturn(0.0);
        fad.maybeReleaseFish(globalBiology.getSpecies(), rng);
        assertTrue(fadBiology.isEmpty());

    }
}