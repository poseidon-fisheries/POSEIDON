/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.DummyFishBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.GlobalCarryingCapacity;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.fillBiomassFad;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.makeBiology;

public class MakeFadSetTest {

    private final GlobalBiology globalBiology =
        new GlobalBiology(new Species("A"), new Species("B"));

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void act() {

        final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final SeaTile seaTile = mock(SeaTile.class);
        final FadMap fadMap = mock(FadMap.class);
        final FadManager fadManager = mock(FadManager.class, RETURNS_DEEP_STUBS);
        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        final Regulation regulation = mock(Regulation.class);
        final Hold hold = mock(Hold.class);

        // Make a full FAD and an empty tile biology
        final double carryingCapacity = 0.0;
        final BiomassLocalBiology fadBiology = makeBiology(globalBiology, carryingCapacity);
        final BiomassAggregatingFad fad = new BiomassAggregatingFad(
            fadManager,
            fadBiology,
            new DummyFishBiomassAttractor(globalBiology.getSize()),
            0,
            new Int2D(1, 1),
            new GlobalCarryingCapacity(carryingCapacity),
            globalBiology.getSpecies().stream().collect(toImmutableMap(identity(), __ -> 0.0))
        );
        fillBiomassFad(fad);
        final VariableBiomassBasedBiology tileBiology =
            makeBiology(globalBiology, carryingCapacity);

        // wire everything together...
        when(seaTile.getBiology()).thenReturn(tileBiology);
        when(seaTile.isWater()).thenReturn(true);
        when(model.getBiology()).thenReturn(globalBiology);
        when(model.getFadMap().getFadTile(any())).thenReturn(Optional.of(seaTile));
        when(fadMap.getFadTile(fad)).thenReturn(Optional.of(seaTile));
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(fisher.grabState()).thenReturn(model);
        when(fisher.grabRandomizer()).thenReturn(random);
        when(fisher.getLocation()).thenReturn(seaTile);
        when(fisher.getHold()).thenReturn(hold);
        when(fisher.getRegulation()).thenReturn(regulation);
        when(fisher.isCheater()).thenReturn(false);
        when(regulation.canFishHere(any(), any(), any())).thenReturn(true);

        // Before the set, FAD biology should be full and tile biology should be empty
        Assertions.assertEquals(Arrays.stream(fad.getBiomass()).sum(), fad.getCarryingCapacity().getTotal(), 0.0);
        Assertions.assertTrue(tileBiology.isEmpty());

        // After a successful set, FAD biology should be empty and tile biology should also be empty
        final PurseSeinerAction fadSetAction = new FadSetAction(fad, fisher, 1);
        when(random.nextDouble()).thenReturn(1.0);
        fadSetAction.act(model, fisher, regulation, fadSetAction.getDuration());
        Assertions.assertTrue(fadBiology.isEmpty());
        Assertions.assertTrue(tileBiology.isEmpty());

        // Now we refill the FAD biology and make an unsuccessful set
        fillBiomassFad(fad);
        when(random.nextDouble()).thenReturn(0.0);
        fadSetAction.act(model, fisher, regulation, fadSetAction.getDuration());

        // After that, the FAD biology should be empty and the tile biology should be full
        Assertions.assertTrue(fadBiology.isEmpty());
        Assertions.assertTrue(tileBiology.isFull());
    }

}
