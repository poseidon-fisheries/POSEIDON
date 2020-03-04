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
import java.util.LinkedList;
import java.util.function.BiFunction;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

public class PossibleRoute {

    private final ImmutableList<Step> steps;
    private final double totalTravelTimeInHours;
    private final int lastTimeStep;

    public PossibleRoute(
        Deque<SeaTile> route,
        int startingTimeStep,
        double hoursPerStep,
        double speedInKph,
        BiFunction<Deque<SeaTile>, Double, ImmutableList<Pair<SeaTile, Double>>> getCumulativeTravelTimeAlongRoute
    ) {
        this.steps =
            getCumulativeTravelTimeAlongRoute.apply(route, speedInKph)
                .stream()
                .map(pair -> new Step(
                    pair.getFirst(),
                    pair.getSecond(),
                    (int) (startingTimeStep + (pair.getSecond() / hoursPerStep))
                ))
                .collect(toImmutableList());
        final Step lastStep = getLast(steps);
        this.totalTravelTimeInHours = lastStep.getCumulativeHours();
        this.lastTimeStep = lastStep.getTimeStep();
    }

    public Route makeRoute(Fisher fisher) {
        final LinkedList<SeaTile> tiles = steps.stream().map(Step::getSeaTile).collect(toCollection(LinkedList::new));
        return new Route(tiles, fisher);
    }

    public ImmutableList<Step> getSteps() { return steps; }

    public int getLastTimeStep() { return lastTimeStep; }

    public double getCost(Fisher fisher) {
        return fisher
            .getAdditionalTripCosts()
            .stream()
            .mapToDouble(cost -> cost.cost(fisher, null, null, 0.0, getTotalTravelTimeInHours()))
            .sum();
    }

    public double getTotalTravelTimeInHours() { return totalTravelTimeInHours; }

    @Override public String toString() {
        return steps.stream().map(Step::toString).collect(joining(" -> "));
    }

    static class Step {

        private final SeaTile seaTile;
        private final double cumulativeHours;
        private final int timeStep;

        public Step(SeaTile seaTile, double cumulativeHours, int timeStep) {
            this.seaTile = seaTile;
            this.cumulativeHours = cumulativeHours;
            this.timeStep = timeStep;
        }

        public SeaTile getSeaTile() { return seaTile; }

        public double getCumulativeHours() { return cumulativeHours; }

        public int getTimeStep() { return timeStep; }

        @Override public String toString() {
            return String.format(
                "[%d: %d, %d (%.2fh)]",
                timeStep,
                seaTile.getGridX(),
                seaTile.getGridY(),
                cumulativeHours
            );
        }
    }
}
