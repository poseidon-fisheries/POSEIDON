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

package uk.ac.ox.poseidon.fishing.tasks.travel;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkState;

import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A leaf {@link VesselTask} that docks the vessel at its current trip's
 * destination, which must be a port, and always succeeds.
 */
@RequiredArgsConstructor
public class DockAtDestination extends VesselTask {

  /** The clock, which dates the docking. */
  @NonNull
  private final Function<? super Vessel, LocalDateTime> currentDateTime;

  /**
   * Docks the vessel at its current trip's destination.
   *
   * @throws IllegalStateException if the destination is not a port, or the
   *                               vessel cannot dock there (see
   *                               {@link Vessel#dockAt})
   */
  @Override
  public Status execute() {
    final Vessel vessel = getVessel();
    final Destination destination = vessel.getCurrentTrip().getDestination();
    checkState(
      destination instanceof Port,
      "Vessel %s cannot dock at %s, which is not a port.",
      vessel.getId(),
      destination
    );
    vessel.dockAt((Port) destination, currentDateTime.apply(vessel));
    return SUCCEEDED;
  }
}
