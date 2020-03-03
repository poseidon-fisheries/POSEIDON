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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.junit.Test;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.fads.SetLimits;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.CUBIC_METRE;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.makeUniformCurrentVectors;
import static uk.ac.ox.oxfish.fisher.strategies.destination.fad.FadSettingRouteSelector.getFadSetsRemaining;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeRoute;

public class FadSettingRouteSelectorTest {

    @Test
    public void test() {

        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);

        final GlobalBiology globalBiology = new GlobalBiology();
        final NauticalMap map = makeCornerPortMap(3, 3, globalBiology);
        final Port port = map.getPorts().getFirst();

        final CurrentVectors currentVectors = makeUniformCurrentVectors(map, new Double2D(0.75, 0), 1);
        final FadMap fadMap = new FadMap(map, currentVectors, globalBiology);

        FadInitializer fadInitializer = new FadInitializer(globalBiology, ImmutableMap.of(), ImmutableMap.of(), 0);
        final FadManager fadManager = new FadManager(fadMap, fadInitializer, Integer.MAX_VALUE, 0, Stream.of());

        fadManager.setFisher(fisher);

        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);

        final FishState fishState = mock(FishState.class);
        when(fishState.getMap()).thenReturn(map);
        when(fishState.getHoursPerStep()).thenReturn(1.0);

        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fisher.getBoat().getSpeedInKph()).thenReturn(1.0);
        when(fisher.getHold().getPercentageFilled()).thenReturn(0.0);
        when(fisher.getHold().getVolume()).thenReturn(Optional.of(getQuantity(1, CUBIC_METRE)));
        when(fisher.getHomePort()).thenReturn(port);
        when(fisher.getLocation()).thenReturn(port.getLocation());
        when(fisher.getRegulation().canFishHere(any(), any(), any(), anyInt())).thenReturn(true);

        final FadSettingRouteSelector routeSelector = new FadSettingRouteSelector(
            fishState, Double.MAX_VALUE, 1, 1
        );

        // Start with no regulations and check that FAD sets are practically unlimited
        assertEquals(Long.MAX_VALUE, getFadSetsRemaining(fadManager));

        // We should not have any possible destinations until FADs are in there
        assertTrue(routeSelector.getPossibleDestinations(fisher, 0).isEmpty());

        // Check that route from port to (2, 2) takes us there and back to port
        assertEquals(
            Optional.of(makeRoute(map, new int[][]{{0, 0}, {1, 1}, {2, 2}, {1, 1}, {0, 0}})),
            routeSelector.getRoute(fisher, port.getLocation(), map.getSeaTile(2, 2))
        );

        final Set<SeaTile> initialFadTiles = Stream.of(
            map.getSeaTile(0, 1),
            map.getSeaTile(1, 0),
            map.getSeaTile(1, 1)
        ).collect(toSet());

        // put a couple of FADs on the map at 1,0 and 1,1.
        final ImmutableSet<Fad> fads = initialFadTiles.stream()
            .map(seaTile -> fadManager.deployFad(seaTile, 0))
            .collect(toImmutableSet());

        // check that only initial positions are reported when not looking ahead
        routeSelector.setNumberOfStepsToLookAheadForFadPositions(0);
        assertEquals(initialFadTiles, routeSelector.getPossibleDestinations(fisher, 0));

        // check that FADs have drifted west when looking 1 step ahead and that
        // the set of possible destinations has correspondingly expanded.
        routeSelector.setNumberOfStepsToLookAheadForFadPositions(3);
        final Set<SeaTile> possibleDestinations = routeSelector.getPossibleDestinations(fisher, 0);
        assertEquals(
            Stream.concat(
                initialFadTiles.stream(),
                Stream.of(
                    map.getSeaTile(2, 0),
                    map.getSeaTile(2, 1)
                )
            ).collect(toSet()),
            possibleDestinations
        );

        final ImmutableList<Route> possibleRoutes =
            routeSelector.getPossibleRoutes(fisher, possibleDestinations, 0);

        // check that all possible routes go back to port
        assertTrue(possibleRoutes.stream().allMatch(route -> route.getRouteDeque().peekLast() == port.getLocation()));

        // given a route with two FADs and a route with one FAD, prefer the one with two FADs
        assertEquals(
            Optional.of(makeRoute(map, new int[][]{{0, 0}, {1, 1}, {2, 1}, {1, 1}, {0, 0}})),
            routeSelector.evaluateRoutes(fisher, possibleRoutes, 0).max(comparingDouble(Entry::getValue)).map(Entry::getKey)
        );

        // No put a limit of just one FAD set
        fadManager.setActionSpecificRegulations(Stream.of(new SetLimits(__ -> {}, ImmutableSortedMap.of(0, 1))));
        assertEquals(1, getFadSetsRemaining(fadManager));
        assertFalse(routeSelector.shouldGoToPort(fisher));

        // Given travel costs and the 1 set limit, we should now prefer the shorter route, catching just one FAD
        final LinkedList<Cost> costs = Stream.of(new HourlyCost(2)).collect(toCollection(LinkedList::new));
        when(fisher.getAdditionalTripCosts()).thenReturn(costs);
        assertEquals(
            Optional.of(makeRoute(map, new int[][]{{0, 0}, {1, 1}, {0, 0}})),
            routeSelector.evaluateRoutes(fisher, possibleRoutes, 0).max(comparingDouble(Entry::getValue)).map(Entry::getKey)
        );

    }

}