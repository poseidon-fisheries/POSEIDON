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

import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * A leaf {@link VesselTask} that records the vessel's arrival in port, ending
 * its current trip's time at sea, and always succeeds.
 */
@RequiredArgsConstructor
public class ArriveInPort extends VesselTask {

  /** The clock, which dates the arrival. */
  @NonNull
  private final Function<? super Vessel, LocalDateTime> currentDateTime;

  /** Records the vessel's arrival in port. */
  @Override
  public Status execute() {
    getVessel().arriveInPort(currentDateTime.apply(getVessel()));
    return SUCCEEDED;
  }
}
