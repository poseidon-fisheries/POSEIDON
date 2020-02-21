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
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Deque;
import java.util.function.BiFunction;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.getLast;

public class Route {

    private final Deque<SeaTile> route;
    private final ImmutableList<RouteStep> steps;
    private final double totalTravelTimeInHours;
    private final int lastTimeStep;

    public Route(
        Deque<SeaTile> route,
        int startingTimeStep,
        double hoursPerStep,
        double speedInKph,
        BiFunction<Deque<SeaTile>, Double, ImmutableList<Pair<SeaTile, Double>>> getCumulativeTravelTimeAlongRoute
    ) {
        this.route = route;
        this.steps =
            getCumulativeTravelTimeAlongRoute.apply(route, speedInKph)
                .stream()
                .map(pair -> new RouteStep(
                    pair.getFirst(),
                    pair.getSecond(),
                    (int) (startingTimeStep + (pair.getSecond() / hoursPerStep))
                ))
                .collect(toImmutableList());
        final RouteStep lastStep = getLast(steps);
        this.totalTravelTimeInHours = lastStep.getCumulativeHours();
        this.lastTimeStep = lastStep.getTimeStep();
    }

    /**
     * Returns the original (mutable!) Deque<SeaTile> from which the Route was created.
     */
    public Deque<SeaTile> getRouteDeque() { return route; }

    public ImmutableList<RouteStep> getSteps() { return steps; }

    public int getLastTimeStep() { return lastTimeStep; }

    public double getCost(Fisher fisher) {
        return fisher
            .getAdditionalTripCosts()
            .stream()
            .mapToDouble(cost -> cost.cost(fisher, null, null, 0.0, getTotalTravelTimeInHours()))
            .sum();
    }

    public double getTotalTravelTimeInHours() { return totalTravelTimeInHours; }

}
