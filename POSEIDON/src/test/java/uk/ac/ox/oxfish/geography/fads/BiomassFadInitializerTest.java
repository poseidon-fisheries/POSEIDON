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

package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.DummyFishBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FixedGlobalCarryingCapacitySupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.ReliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO;
import uk.ac.ox.oxfish.model.FishState;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.function.Function.identity;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BiomassFadInitializerTest {

    @Test
    public void fadBiomassInitializedToZero() {
        final Species speciesA = new Species("A");
        final Species speciesB = new Species("B");
        final GlobalBiology globalBiology = new GlobalBiology(speciesA, speciesB);
        final BiomassFadInitializer fadInitializer = new BiomassFadInitializer(
            globalBiology,
            new DummyFishBiomassAttractor(globalBiology.getSize()),
            () -> 0,
            new FixedGlobalCarryingCapacitySupplier(POSITIVE_INFINITY),
            globalBiology.getSpecies().stream().collect(toImmutableMap(identity(), __ -> 0.0))
        );
        final FadMap fadMap =
            new FadMap(
                mock(NauticalMap.class),
                mock(CurrentVectorsEPO.class),
                globalBiology,
                BiomassLocalBiology.class
            );
        final FadManager fadManager =
            new FadManager(
                fadMap,
                fadInitializer,
                null,
                new ReliableFishValueCalculator(globalBiology)
            );
        final SeaTile seaTile = mock(SeaTile.class);
        when(seaTile.getGridX()).thenReturn(0);
        when(seaTile.getGridY()).thenReturn(0);
        final FishState fishState = mock(FishState.class);
        when(fishState.getBiology()).thenReturn(globalBiology);
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(seaTile);
        when(fisher.grabState()).thenReturn(fishState);
        fadManager.setFisher(fisher);

        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final BiomassAggregatingFad fad = fadInitializer.makeFad(fadManager, null, seaTile, rng);
        for (final Species species : globalBiology.getSpecies()) {
            Assertions.assertEquals(fad.getBiology().getBiomass(species), 0, 0);
        }
    }

}
