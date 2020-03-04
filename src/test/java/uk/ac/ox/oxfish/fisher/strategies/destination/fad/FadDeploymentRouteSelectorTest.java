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
import com.google.common.collect.Streams;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Map.Entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.strategies.destination.fad.AbstractRouteSelector.getTimeStepRange;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeRoute;

public class FadDeploymentRouteSelectorTest {
    @SuppressWarnings("UnstableApiUsage") @Test
    public void test() {

        final FadManager fadManager = mock(FadManager.class);
        final ActiveActionRegulations activeActionRegulations = new ActiveActionRegulations();
        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        final Regulation regulation = mock(Regulation.class);

        final NauticalMap map = makeCornerPortMap(3, 3);
        final Port port = map.getPorts().getFirst();
        final FishState fishState = mock(FishState.class);

        when(fishState.getMap()).thenReturn(map);
        when(fishState.getHoursPerStep()).thenReturn(1.0);
        when(fadManager.getActionSpecificRegulations()).thenReturn(activeActionRegulations);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fisher.getBoat().getSpeedInKph()).thenReturn(1.0);
        when(fisher.getLocation()).thenReturn(port.getLocation());
        when(fisher.getRegulation()).thenReturn(regulation);

        final FadDeploymentRouteSelector routeSelector =
            new FadDeploymentRouteSelector(fishState, 0, 1);

        // just check that deployment location values are empty by default
        assertEquals(ImmutableMap.<SeaTile, Double>of(), routeSelector.getDeploymentLocationValues());

        // make sure no possible destinations are returned as long as max travel time is 0
        final Set<SeaTile> emptyDestinationSet = routeSelector.getPossibleDestinations(fisher, 0);
        assertTrue(emptyDestinationSet.isEmpty());
        assertTrue(routeSelector.getPossibleRoutes(fisher, emptyDestinationSet, 0).isEmpty());

        final ImmutableSet<SeaTile> deploymentLocations = ImmutableSet.of(
            map.getSeaTile(0, 1),
            map.getSeaTile(0, 2),
            map.getSeaTile(2, 2)
        );
        final HashMap<SeaTile, Double> deploymentLocationValues = new HashMap<>(ImmutableMap.of(
            map.getSeaTile(0, 1), 3.0,
            map.getSeaTile(0, 2), 1.0,
            map.getSeaTile(2, 2), 1.0
        ));
        routeSelector.setDeploymentLocationValues(deploymentLocationValues);

        routeSelector.setMaxTravelTimeInHours(2.0);
        final Set<SeaTile> possibleDestinations = routeSelector.getPossibleDestinations(fisher, 0);
        assertEquals(deploymentLocations, possibleDestinations);

        final ImmutableList<PossibleRoute> possibleRoutes =
            routeSelector.getPossibleRoutes(fisher, possibleDestinations, 0);
        assertEquals(ImmutableList.of(0, 1, 2), getTimeStepRange(0, possibleRoutes));

        final Route shortRoute = new Route(makeRoute(map, new int[][]{{0, 0}, {0, 1}}), fisher);
        final Route longRoute = new Route(makeRoute(map, new int[][]{{0, 0}, {0, 1}, {0, 2}}), fisher);
        // the route going to 2,2 should be excluded because travel time > 2.0h
        assertEquals(
            ImmutableSet.of(shortRoute, longRoute),
            possibleRoutes.stream().map(possibleRoute -> possibleRoute.makeRoute(fisher)).collect(toImmutableSet())
        );

        // same as previous test, but with strings
        assertEquals(
            ImmutableSet.of(
                "[0: 0, 0 (0.00h)] -> [1: 0, 1 (1.00h)]",
                "[0: 0, 0 (0.00h)] -> [1: 0, 1 (1.00h)] -> [2: 0, 2 (2.00h)]"
            ),
            possibleRoutes.stream().map(PossibleRoute::toString).collect(toImmutableSet())
        );

        when(regulation.canFishHere(any(), any(), any(), anyInt())).thenReturn(true);
        final ImmutableMap<Route, Double> routeValues =
            routeSelector.evaluateRoutes(fisher, possibleRoutes, 0)
                .collect(toImmutableMap(Entry::getKey, Entry::getValue));
        assertEquals(ImmutableMap.of(shortRoute, 3.0, longRoute, 4.0), routeValues);

        final MersenneTwisterFast rng = new MersenneTwisterFast();

        final Map<Route, Long> routeSelectionCounts = Stream
            .generate(() -> routeSelector.selectRoute(fisher, 0, rng))
            .flatMap(Streams::stream)
            .limit(100)
            .collect(groupingBy(identity(), counting()));

        // with no travel costs the shortest route should be picked roughly 43% of the time
        assertEquals(
            routeValues.get(shortRoute) / routeValues.values().stream().mapToDouble(Double::doubleValue).sum(),
            routeSelectionCounts.get(shortRoute).doubleValue() / routeSelectionCounts.get(longRoute),
            5
        );

        // never pick longer route when adding costs
        final LinkedList<Cost> costs = Stream.of(new HourlyCost(2)).collect(toCollection(LinkedList::new));
        when(fisher.getAdditionalTripCosts()).thenReturn(costs);
        assertEquals(
            ImmutableMap.of(shortRoute, 1.0, longRoute, 0.0),
            routeSelector.evaluateRoutes(fisher, possibleRoutes, 0)
                .collect(toImmutableMap(Entry::getKey, Entry::getValue))
        );
        Stream
            .generate(() -> routeSelector.selectRoute(fisher, 0, rng))
            .flatMap(Streams::stream)
            .limit(10)
            .forEach(route -> assertEquals(shortRoute, route));

        // when FAD deployment is not permitted at 0, 1
        // the selector should pick the longer route despite costs
        routeSelector.getDeploymentLocationValues().put(map.getSeaTile(0, 2), 4.01);
        when(regulation.canFishHere(any(), eq(map.getSeaTile(0, 1)), any(), anyInt())).thenReturn(false);
        Stream
            .generate(() -> routeSelector.selectRoute(fisher, 0, rng))
            .flatMap(Streams::stream)
            .limit(10)
            .forEach(route -> assertEquals(longRoute, route));
    }

}