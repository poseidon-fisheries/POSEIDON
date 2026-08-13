/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels.providers;

import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class TravelTimeToPortViaDestination implements Function<Int2D, Duration> {

    private final Vessel vessel;
    private final Int2D homePortCell;
    private final GridPathFinder pathFinder;
    private final DistanceCalculator distanceCalculator;

    @Override
    public Duration apply(
        final Int2D destination
    ) {
        final double cruisingSpeedInKph = vessel.getEngine().getCruisingSpeedInKph();

        final List<Int2D> pathToDestination =
            pathFinder.getPath(
                vessel.getCell(),
                destination
            ).orElseThrow(() -> new RuntimeException(
                "No path from current vessel location " +
                    vessel.getCell() +
                    " to candidate destination " +
                    destination
            ));
        final List<Int2D> pathBackToPort =
            pathFinder.getPath(
                destination,
                homePortCell
            ).orElseThrow(() -> new RuntimeException(
                "No path from candidate destination " +
                    destination +
                    " back to home port " +
                    homePortCell
            ));

        return distanceCalculator.travelDuration(pathToDestination, cruisingSpeedInKph)
            .plus(distanceCalculator.travelDuration(pathBackToPort, cruisingSpeedInKph));
    }
}
