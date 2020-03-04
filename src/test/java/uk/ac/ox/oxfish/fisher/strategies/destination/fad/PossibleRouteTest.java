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
import com.google.common.collect.Streams;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toCollection;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class PossibleRouteTest {

    @SuppressWarnings("UnstableApiUsage") @Test
    public void test() {

        final LinkedList<SeaTile> tileDeque = Stream
            .generate(() -> mock(SeaTile.class))
            .limit(5)
            .collect(toCollection(LinkedList::new));

        BiFunction<Deque<SeaTile>, Double, ImmutableList<Pair<SeaTile, Double>>> travelTimes =
            (tiles, __) -> Streams
                .mapWithIndex(tiles.stream(), (tile, i) -> new Pair<>(tile, (double) i))
                .collect(toImmutableList());

        PossibleRoute route = new PossibleRoute(tileDeque, 0, 1, 1, travelTimes);

        assertEquals(tileDeque, route.getSteps().stream().map(PossibleRoute.Step::getSeaTile).collect(toCollection(LinkedList::new)));
        assertEquals(new Route(tileDeque, null), route.makeRoute(null));
        assertEquals(4, route.getLastTimeStep());
        assertEquals(4.0, route.getTotalTravelTimeInHours(), EPSILON);
        assertEquals(ImmutableList.of(0, 1, 2, 3, 4), route.getSteps().stream().map(PossibleRoute.Step::getTimeStep).collect(toImmutableList()));
        assertEquals(ImmutableList.of(0d, 1d, 2d, 3d, 4d), route.getSteps().stream().map(PossibleRoute.Step::getCumulativeHours).collect(toImmutableList()));

        Fisher fisher = mock(Fisher.class);
        when(fisher.getAdditionalTripCosts()).thenReturn(new LinkedList<>(ImmutableList.of(new HourlyCost(2))));
        assertEquals(8.0, route.getCost(fisher), EPSILON);

    }
}