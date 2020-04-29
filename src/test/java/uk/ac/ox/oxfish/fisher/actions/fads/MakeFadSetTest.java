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

package uk.ac.ox.oxfish.fisher.actions.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.fillBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.makeBiology;

public class MakeFadSetTest {

    private final GlobalBiology globalBiology = new GlobalBiology(new Species("A"), new Species("B"));

    @Test
    public void act() {

        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        SeaTile seaTile = mock(SeaTile.class);
        FadMap fadMap = mock(FadMap.class);
        FadManager fadManager = mock(FadManager.class);
        PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        Regulation regulation = mock(Regulation.class);
        final Hold hold = mock(Hold.class);

        // Make a full FAD and an empty tile biology
        final double carryingCapacity = 0.0;
        final BiomassLocalBiology fadBiology = makeBiology(globalBiology, carryingCapacity);
        fillBiology(fadBiology);
        final Fad fad = new Fad(fadManager, fadBiology, ImmutableMap.of(), 0, 0);
        VariableBiomassBasedBiology tileBiology = makeBiology(globalBiology, carryingCapacity);

        // wire everything together...
        when(seaTile.getBiology()).thenReturn(tileBiology);
        when(seaTile.isWater()).thenReturn(true);
        when(model.getBiology()).thenReturn(globalBiology);
        when(model.getRandom()).thenReturn(random);
        when(model.getFadMap().getFadTile(any())).thenReturn(Optional.of(seaTile));
        when(fadMap.getFadTile(fad)).thenReturn(Optional.of(seaTile));
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(fadManager.getActionSpecificRegulations()).thenReturn(new ActiveActionRegulations());
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(fisher.getLocation()).thenReturn(seaTile);
        when(fisher.getHold()).thenReturn(hold);
        when(fisher.getRegulation()).thenReturn(regulation);
        when(fisher.isCheater()).thenReturn(false);
        when(regulation.canFishHere(any(), any(), any())).thenReturn(true);

        // Before the set, FAD biology should be full and tile biology should be empty
        assertTrue(fadBiology.isFull());
        assertTrue(tileBiology.isEmpty());

        // After a successful set, FAD biology should be empty and tile biology should also be empty
        final MakeFadSet makeFadSet = new MakeFadSet(model, fisher, fad);
        when(random.nextDouble()).thenReturn(1.0);
        makeFadSet.act(model, fisher, regulation, 0);
        assertTrue(fadBiology.isEmpty());
        assertTrue(tileBiology.isEmpty());

        // Now we refill the FAD biology and make an unsuccessful set
        fillBiology(fadBiology);
        when(random.nextDouble()).thenReturn(0.0);
        makeFadSet.act(model, fisher, regulation, 0);

        // After that, the FAD biology should be empty and the tile biology should be full
        assertTrue(fadBiology.isEmpty());
        assertTrue(tileBiology.isFull());
    }
}