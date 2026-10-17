/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.tasks.destinations;

import static com.badlogic.gdx.ai.btree.Task.Status.FAILED;
import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.AgentTask;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * Redirects the vessel's current trip to whatever destination
 * {@code destination} gives for the vessel; fails, leaving the trip's
 * destination unchanged, if it gives none. The mid-trip counterpart of
 * {@link StartTrip}: within a trip, a failure ends a loop of fishing
 * operations.
 */
@RequiredArgsConstructor
public class ChooseNextDestination extends AgentTask<Vessel> {

  private final @NonNull Function<? super Vessel, Optional<Int2D>> destination;

  /**
   * @return {@link Status#SUCCEEDED} if a destination was found and the trip
   *         redirected to it, else {@link Status#FAILED}
   * @throws IllegalStateException if the vessel is not on a trip
   */
  @Override
  public Status execute() {
    final Trip currentTrip = getAgent().getCurrentTrip();
    checkState(
      currentTrip != null,
      "Vessel %s is not on a trip.",
      getAgent().getId()
    );
    return destination
      .apply(getAgent())
      .map(nextDestination -> {
        currentTrip.setDestination(nextDestination);
        return SUCCEEDED;
      })
      .orElse(FAILED);
  }
}
