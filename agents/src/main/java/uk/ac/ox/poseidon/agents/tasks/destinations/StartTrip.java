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

import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.AgentTask;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * Starts a vessel's trip towards whatever destination {@code destination} gives
 * for the vessel; fails, without starting a trip, if it gives none.
 */
@RequiredArgsConstructor
public class StartTrip extends AgentTask<Vessel> {

  private final Function<? super Vessel, Optional<Int2D>> destination;

  /**
   * @return {@link Status#SUCCEEDED} if a destination was found and the trip
   *         started, else {@link Status#FAILED}
   */
  @Override
  public Status execute() {
    return destination
      .apply(getAgent())
      .map(firstDestination -> {
        getAgent().startTrip(firstDestination);
        return SUCCEEDED;
      })
      .orElse(FAILED);
  }
}
