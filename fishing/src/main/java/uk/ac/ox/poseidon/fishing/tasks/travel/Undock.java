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

package uk.ac.ox.poseidon.fishing.tasks.travel;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkState;

import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * A leaf {@link VesselTask} that undocks the vessel, ending a stop of its
 * current trip, and always succeeds. A trip's start undocks by itself, so this
 * task runs only within a trip.
 */
@RequiredArgsConstructor
public class Undock extends VesselTask {

  /** The clock, which dates the undocking. */
  @NonNull
  private final Function<? super Vessel, LocalDateTime> currentDateTime;

  /**
   * Undocks the vessel.
   *
   * @throws IllegalStateException if the vessel is on no trip, or is not
   *                               docked
   */
  @Override
  public Status execute() {
    final Vessel vessel = getVessel();
    checkState(
      vessel.getCurrentTrip() != null,
      "Vessel %s cannot undock outside a trip: a trip's start undocks it.",
      vessel.getId()
    );
    vessel.undock(currentDateTime.apply(vessel));
    return SUCCEEDED;
  }
}
