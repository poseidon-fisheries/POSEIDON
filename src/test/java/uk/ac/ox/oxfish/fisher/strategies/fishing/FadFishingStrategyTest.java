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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Before;
import org.junit.Test;
import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.fad.FadDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FadFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.unit.Units.HOUR;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class FadFishingStrategyTest {

    private final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
    private final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
    private final FadDestinationStrategy fadDestinationStrategy =
        mock(FadDestinationStrategy.class, RETURNS_DEEP_STUBS);
    private final FadFishingStrategyFactory fadFishingStrategyFactory = new FadFishingStrategyFactory();
    private final FishState fishState = mock(FishState.class);
    private final FadFishingStrategy fadFishingStrategy = fadFishingStrategyFactory.apply(fishState);
    private final SeaTile seaTile = mock(SeaTile.class);
    private final FadManager fadManager = mock(FadManager.class);
    private final FadMap fadMap = mock(FadMap.class);
    private final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
    private final TripRecord tripRecord = mock(TripRecord.class);

    @Before
    public void setUp() {
        when(fishState.getRandom()).thenReturn(rng);
        when(fishState.getFadMap()).thenReturn(fadMap);
        when(fadManager.getActionSpecificRegulations()).thenReturn(new ActiveActionRegulations());
        when(fadManager.getNumFadsInStock()).thenReturn(1);
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(seaTile.isWater()).thenReturn(true);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fisher.getLocation()).thenReturn(seaTile);
        when(fisher.getDestinationStrategy()).thenReturn(fadDestinationStrategy);
        when(fisher.getRegulation().canFishHere(any(), any(), any(), anyInt())).thenReturn(true);
    }

    @Test
    public void shouldFish_FalseWhenOnLand() {
        when(fisher.getLocation().isLand()).thenReturn(true);
        assertFalse(fadFishingStrategy.shouldFish(fisher, rng, fishState, tripRecord));
    }

    @Test
    public void maybeDeployFad_ShouldReturnEmptyWhenNoValueForCurrentLocation() {
        when(fadDestinationStrategy.getFadDeploymentRouteSelector().getDeploymentLocationValues())
            .thenReturn(ImmutableMap.of());
        assertFalse(fadFishingStrategy.maybeDeployFad(fishState, fisher).isPresent());
    }

    @Test
    public void maybeDeployFad_ShouldReturnEmptyWhenUnlucky() {
        when(fadDestinationStrategy.getFadDeploymentRouteSelector().getDeploymentLocationValues())
            .thenReturn(ImmutableMap.of(seaTile, 1.0));
        when(rng.nextBoolean(anyDouble())).thenReturn(false);
        assertFalse(fadFishingStrategy.maybeDeployFad(fishState, fisher).isPresent());
    }

    @Test
    public void actionSequence() {
        when(fadDestinationStrategy.getFadDeploymentRouteSelector().getDeploymentLocationValues())
            .thenReturn(ImmutableMap.of(seaTile, 1.0));

        // The first action, when allowed by the rng, should be DeployFad
        when(rng.nextBoolean(anyDouble())).thenReturn(true);
        final Optional<? extends PurseSeinerAction> action1 = fadFishingStrategy.updateAndReturnNextAction(fishState, fisher);
        assertTrue(action1.isPresent());
        assertTrue(action1.get() instanceof DeployFad);
        assertTrue(fadFishingStrategy.shouldFish(fisher, rng, fishState, tripRecord));

        // The second action, when nothing changes, should still be the same DeployFad
        final Optional<? extends PurseSeinerAction> action2 = fadFishingStrategy.updateAndReturnNextAction(fishState, fisher);
        assertTrue(action2.isPresent());
        assertSame(action2.get(), action1.get());
        assertTrue(fadFishingStrategy.shouldFish(fisher, rng, fishState, tripRecord));

        fadFishingStrategy.act(
            fishState, fisher, mock(Regulation.class), asDouble(action2.get().getDuration(), HOUR)
        );
        // Next, we should get MakeFadSet
        when(rng.nextBoolean(anyDouble())).thenReturn(false, true);

        // Make FAD on which to set
        final Fad fad = mock(Fad.class);
        when(fadManager.getFadsHere()).thenReturn(new Bag(new Object[]{fad}));
        when(fadMap.getFadTile(any())).thenReturn(Optional.of(seaTile));
        final Optional<? extends PurseSeinerAction> action3 = fadFishingStrategy.updateAndReturnNextAction(fishState, fisher);
        assertTrue(action3.isPresent());
        assertTrue(action3.get() instanceof MakeFadSet);
        assertTrue(fadFishingStrategy.shouldFish(fisher, rng, fishState, tripRecord));

    }

}