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

import ec.util.MersenneTwisterFast;
import junit.framework.TestCase;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FadManagerTest extends TestCase {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testFadsGoBackInStockAfterSet() {

        final FadInitializer<BiomassLocalBiology, BiomassAggregatingFad> fadInitializer =
            (fadManager, owner, initialLocation, rng) -> {
                final BiomassAggregatingFad fad = mock(BiomassAggregatingFad.class);
                when(fad.getOwner()).thenReturn(fadManager);
                when(fad.getBiology()).thenReturn(new BiomassLocalBiology(new double[]{0}));
                when(fad.getLocation()).thenReturn(initialLocation);
                return fad;
            };

        final FadMap fadMap = mock(FadMap.class);

        final FadManager<BiomassLocalBiology> fadManager =
            new FadManager(fadMap, fadInitializer, null);

        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);

        final GlobalBiology globalBiology = GlobalBiology.genericListOfSpecies(1);
        final FishState fishState = mock(FishState.class);
        when(fishState.getStep()).thenReturn(1);
        when(fishState.getFadMap()).thenReturn(fadMap);
        when(fishState.getBiology()).thenReturn(globalBiology);

        final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);

        final SeaTile seaTile = mock(SeaTile.class);
        final Regulation anarchy = new Anarchy();
        final Fisher fisher = mock(Fisher.class);
        when(fisher.grabState()).thenReturn(fishState);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fisher.grabRandomizer()).thenReturn(rng);
        when(fisher.getRegulation()).thenReturn(anarchy);
        when(fisher.getLocation()).thenReturn(seaTile);

        fadManager.setFisher(fisher);
        fadManager.setNumFadsInStock(10);
        final BiomassAggregatingFad fad1 = (BiomassAggregatingFad) fadManager.deployFadInCenterOfTile(seaTile, rng);

        assertEquals(9, fadManager.getNumFadsInStock());
        assertEquals(1, fadManager.getNumDeployedFads());

        // try a successful set
        when(rng.nextDouble()).thenReturn(1.0);
        new FadSetAction(fad1, fisher, 1.0)
            .act(fishState, fadManager.getFisher(), anarchy, 24);
        assertEquals(10, fadManager.getNumFadsInStock());

        final BiomassAggregatingFad fad2 = (BiomassAggregatingFad) fadManager.deployFadInCenterOfTile(seaTile, rng);
        assertEquals(9, fadManager.getNumFadsInStock());
        assertEquals(2, fadManager.getNumDeployedFads());

        // try with a failed set
        when(rng.nextDouble()).thenReturn(1.0);
        new FadSetAction(fad2, fisher, 1.0)
            .act(fishState, fadManager.getFisher(), anarchy, 24);
        assertEquals(10, fadManager.getNumFadsInStock());

    }
}