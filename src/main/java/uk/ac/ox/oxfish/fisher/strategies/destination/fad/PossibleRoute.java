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
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;

import java.util.Deque;
import java.util.LinkedList;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

public class PossibleRoute {

    private final ImmutableList<Step> steps;
    private final double totalTravelTimeInHours;
    private final int lastTimeStep;
    private final double totalTravelTimeAndBackToPortInHours;

    public PossibleRoute(
        Deque<SeaTile> route,
        int startingTimeStep,
        double hoursPerStep,
        double speedInKph,
        NauticalMap map,
        Port homePort
    ) {
        this.steps =
            map.cumulativeTravelTimeAlongRouteInHours(route, speedInKph)
                .stream()
                .map(entry -> new Step(
                    entry.getKey(),
                    entry.getValue(),
                    (int) (startingTimeStep + (entry.getValue() / hoursPerStep))
                ))
                .collect(toImmutableList());
        final Step lastStep = getLast(steps);
        this.totalTravelTimeInHours = lastStep.getCumulativeHours();

        this.totalTravelTimeAndBackToPortInHours =
            getLast(map.cumulativeTravelTimeAlongRouteInHours(
                map.getRoute(getLast(steps).seaTile, homePort.getLocation()),
                speedInKph
            )).getValue();

        this.lastTimeStep = lastStep.getTimeStep();
    }

    public Route makeRoute(Fisher fisher) {
        final LinkedList<SeaTile> tiles = steps.stream().map(Step::getSeaTile).collect(toCollection(LinkedList::new));
        return new Route(tiles, fisher);
    }

    public ImmutableList<Step> getSteps() { return steps; }

    public int getLastTimeStep() { return lastTimeStep; }

    public double getCost(Fisher fisher) {
        // We include the travel time back to port in the cost of the route since,
        // even if it's not the last leg of the trip, this cost will have to be paid
        // eventually and fishers have to take it into account when making decisions
        return fisher
            .getAdditionalTripCosts()
            .stream()
            .mapToDouble(cost ->
                cost.cost(fisher, null, null, 0.0, getTotalTravelTimeAndBackToPortInHours())
            )
            .sum();
    }

    double getTotalTravelTimeAndBackToPortInHours() { return totalTravelTimeAndBackToPortInHours; }

    public double getTotalTravelTimeInHours() { return totalTravelTimeInHours; }

    @Override public String toString() {
        return steps.stream().map(Step::toString).collect(joining(" -> "));
    }

    static class Step {

        private final SeaTile seaTile;
        private final double cumulativeHours;
        private final int timeStep;

        Step(SeaTile seaTile, double cumulativeHours, int timeStep) {
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
