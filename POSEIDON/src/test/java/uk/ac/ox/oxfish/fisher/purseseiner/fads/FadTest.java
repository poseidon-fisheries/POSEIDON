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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;

import java.util.Arrays;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.fillBiomassFad;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.makeBiology;

class FadTest {

    private final GlobalBiology globalBiology =
        new GlobalBiology(new Species("A"), new Species("B"));

    @Test
    void releaseFish() {

        // Make a full FAD
        final BiomassLocalBiology fadBiology = makeBiology(globalBiology, Double.POSITIVE_INFINITY);
        final FadManager fadManager = mock(FadManager.class, RETURNS_DEEP_STUBS);
        final BiomassAggregatingFad fad = new BiomassAggregatingFad(
            fadManager,
            fadBiology,
            new DummyFishBiomassAttractor(globalBiology.getSize()),
            0,
            new Int2D(),
            new GlobalCarryingCapacity(1.5),
            // TODO: release probabilities are set to zero for now but, this will need to be modified for a proper test
            globalBiology.getSpecies().stream().collect(toImmutableMap(identity(), __ -> 0.0))
        );
        fillBiomassFad(fad);

        // ...and an empty tile biology, with a carrying capacity of 1.0:
        final VariableBiomassBasedBiology tileBiology = makeBiology(globalBiology, 1.0);

        // generate failed attempt to release the FAD's fish into the tile biology
        final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
        when(rng.nextDouble()).thenReturn(1.0);
        fad.maybeReleaseFishIntoTile(tileBiology, rng);
        // check that the FAD is still full and the tile biology is still empty
        assertEquals(Arrays.stream(fad.getBiomass()).sum(), fad.getCarryingCapacity().getTotal(), 0.0);
        assertTrue(tileBiology.isEmpty());

        // release the FAD's fish into the tile biology
        when(rng.nextDouble()).thenReturn(0.0);
        fad.maybeReleaseFishIntoTile(tileBiology, rng);
        // Check that the FAD is now empty and the tile has received the fish
        assertTrue(fadBiology.isEmpty());
        assertEquals(tileBiology.getTotalBiomass(), fad.getCarryingCapacity().getTotal(), 0);

        // Refill the FAD and release another batch of FAD fish into the tile biology
        fillBiomassFad(fad);
        fad.maybeReleaseFishIntoTile(tileBiology, rng);

        // Check that the FAD is now empty and the tile is now at full carrying capacity
        assertTrue(fadBiology.isEmpty());
        assertTrue(tileBiology.isFull());

        // Fill the FAD one last time and release the fish to nowhere
        fillBiomassFad(fad);
        when(rng.nextDouble()).thenReturn(1.0);
        fad.maybeReleaseFishIntoTheVoid(rng);
        Assertions.assertFalse(fadBiology.isEmpty());
        when(rng.nextDouble()).thenReturn(0.0);
        fad.maybeReleaseFishIntoTheVoid(rng);
        assertTrue(fadBiology.isEmpty());

    }

    @Test
    void testAggregateFish() {
        final BiomassLocalBiology fadBiology = makeBiology(globalBiology, Double.POSITIVE_INFINITY);
        final FadManager fadManager = mock(FadManager.class, RETURNS_DEEP_STUBS);
        final BiomassAggregatingFad fad = new BiomassAggregatingFad(
            fadManager,
            fadBiology,
            new DummyFishBiomassAttractor(globalBiology.getSize()),
            10,
            new Int2D(),
            new GlobalCarryingCapacity(1.5),
            globalBiology.getSpecies().stream().collect(toImmutableMap(identity(), __ -> 0.0))
        );
        Assertions.assertNull(fad.getStepOfFirstAttraction());
        Assertions.assertNull(fad.getStepsBeforeFirstAttraction());
        final BiomassLocalBiology cellBiology = makeBiology(globalBiology, Double.POSITIVE_INFINITY);
        globalBiology.getSpecies().forEach(s -> cellBiology.setCurrentBiomass(s, 1));
        fad.aggregateFish(cellBiology, globalBiology, 20);
        assertEquals(Integer.valueOf(20), fad.getStepOfFirstAttraction());
        assertEquals(Integer.valueOf(10), fad.getStepsBeforeFirstAttraction());
    }
}
