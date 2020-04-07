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

package uk.ac.ox.oxfish.fisher.strategies.destination.fad;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.makeUniformCurrentVectors;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeRoute;

public class FadGravityDestinationStrategyTest {

    @Test
    public void test() {

        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        final NauticalMap map = makeCornerPortMap(3, 3);
        final Port port = map.getPorts().getFirst();
        when(fisher.isAtPort()).thenReturn(true);
        when(fisher.getLocation()).thenReturn(port.getLocation());
        when(fisher.canAndWantToFishHere()).thenReturn(false);

        final FadDeploymentRouteSelector fadDeploymentRouteSelector = mock(FadDeploymentRouteSelector.class);
        when(fadDeploymentRouteSelector.selectRoute(any(), anyInt(), any()))
            .thenAnswer(__ -> Optional.of(new Route(makeRoute(map, new int[][]{{0, 0}, {0, 1}}), fisher)));

        final FadGravityDestinationStrategy fadGravityDestinationStrategy =
            new FadGravityDestinationStrategy(1, fadDeploymentRouteSelector);

        assertEquals(fadDeploymentRouteSelector, fadGravityDestinationStrategy.getFadDeploymentRouteSelector());

        final GlobalBiology globalBiology = new GlobalBiology();
        final CurrentVectors currentVectors = makeUniformCurrentVectors(map, new Double2D(0, 1), 1);
        final FadMap fadMap = new FadMap(map, currentVectors, globalBiology);
        final FadInitializer fadInitializer = new FadInitializer(globalBiology, ImmutableMap.of(), ImmutableMap.of(), 0);
        final FadManager fadManager = new FadManager(fadMap, fadInitializer, Integer.MAX_VALUE, 0, Stream.of());
        fadManager.setFisher(fisher);

        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(fisher.getGear()).thenReturn(purseSeineGear);

        final MersenneTwisterFast rng = new MersenneTwisterFast();
        when(fisher.grabRandomizer()).thenReturn(rng);
        final FishState fishState = mock(FishState.class);
        when(fishState.getStep()).thenReturn(0);
        when(fishState.getMap()).thenReturn(map);

        // first destination should be our FAD deployment destination
        final SeaTile dest01 = fadGravityDestinationStrategy.chooseDestination(fisher, rng, fishState, null);
        assertEquals(map.getSeaTile(0, 1), dest01);

        when(fisher.isAtPort()).thenReturn(false);
        when(fisher.getLocation()).thenReturn(dest01);

        // put one FAD on the map and check that the strategy heads toward it
        fadManager.deployFad(map.getSeaTile(1, 1), 0);
        final SeaTile dest11 = fadGravityDestinationStrategy.chooseDestination(fisher, rng, fishState, null);
        assertEquals(map.getSeaTile(1, 1), dest11);
        when(fisher.getLocation()).thenReturn(dest11);

        // have the FAD drift for one step and check that the strategy follows it
        fadMap.getDriftingObjectsMap().applyDrift(1);
        final SeaTile dest12 = fadGravityDestinationStrategy.chooseDestination(fisher, rng, fishState, null);
        assertEquals(map.getSeaTile(1, 2), dest12);

    }
}