/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.regulations;

import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.agents.behaviours.fishing.DummyFishingAction;
import uk.ac.ox.poseidon.agents.regulations.Regulations;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.Distance;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class FishingLocationLegalityChecker implements Predicate<Int2D> {

    private final Regulations regulations;
    private final GridPathFinder pathFinder;
    private final Distance distance;
    private final Supplier<LocalDateTime> currenDateTimeSupplier;
    private final Vessel vessel;

    public boolean test(final Int2D fishingLocation) {
        return regulations.isPermitted(makeAction(fishingLocation));
    }

    private Action makeAction(final Int2D fishingLocation) {
        final List<Int2D> pathToFishingLocation =
            pathFinder.getPath(
                vessel.getCurrentCell(),
                fishingLocation
            ).orElseThrow(() -> new RuntimeException(
                "No path from current vessel location " +
                    vessel.getCurrentCell() +
                    " to fishing location " +
                    fishingLocation
            ));
        final Duration travelDuration =
            distance.travelDuration(
                pathToFishingLocation,
                vessel.getCruisingSpeed()
            );

        return new DummyFishingAction(
            currenDateTimeSupplier.get().plus(travelDuration),
            vessel,
            fishingLocation
        );
    }
}
