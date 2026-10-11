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

package uk.ac.ox.poseidon.agents.regulations.predicates;

import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.regulations.Regulations;

import java.time.Duration;

/**
 * Factories for predicates over candidate fishing locations, built on the
 * {@link uk.ac.ox.poseidon.regulations} model.
 */
public class Factories {

    private Factories() {}

    /**
     * @param regulations     the regulations a candidate fishing action must be permitted by
     * @param pathFinder      finds the path from the vessel's current cell to the candidate
     * @param distance        gives the travel time along that path
     * @param fishingDuration the typical duration of a fishing action, which the candidate action
     *                        lasts (see {@link FishingLocationLegalityChecker})
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link FishingLocationLegalityChecker}
     * @see FishingLocationLegalityCheckerFactory
     */
    public static FishingLocationLegalityCheckerFactory fishingLocationLegalityChecker(
        final Factory<
            ? super VesselScope,
            ? extends Regulations<? super ExtendedFishingAction>
            > regulations,
        final Factory<? super VesselScope, ? extends GridPathFinder> pathFinder,
        final Factory<? super VesselScope, ? extends DistanceCalculator> distance,
        final Factory<? super VesselScope, ? extends Duration> fishingDuration
    ) {
        return new FishingLocationLegalityCheckerFactory(
            regulations,
            pathFinder,
            distance,
            fishingDuration
        );
    }
}
