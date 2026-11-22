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

package uk.ac.ox.poseidon.fishing.vessels.values;

import static com.google.common.base.Preconditions.checkState;

import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.vessels.PortStayStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Provides when a docked vessel's current stay at port started: when it last
 * docked or became present, or, if it has done neither since it was created,
 * when it was created, as a vessel starts docked at its home port if it has
 * one. It listens to the vessel's {@link PortStayStartEvent}s, so it must be
 * registered on the event manager of the vessel it is asked about, and be given
 * the date-time it is created at as the start of the vessel's first stay.
 */
@AllArgsConstructor
public class CurrentPortStayStartDateTime
  implements Function<Vessel, LocalDateTime>, Listener<PortStayStartEvent>
{

  /** When the vessel's current, or last, stay at port started. */
  @NonNull
  private LocalDateTime portStayStartDateTime;

  @Override
  public Class<PortStayStartEvent> getEventClass() {
    return PortStayStartEvent.class;
  }

  @Override
  public void receive(final PortStayStartEvent event) {
    portStayStartDateTime = event.getDateTime();
  }

  /**
   * @throws IllegalStateException if the vessel is not docked
   */
  @Override
  public LocalDateTime apply(final Vessel vessel) {
    checkState(vessel.isDocked(), "Vessel %s is not docked.", vessel.getId());
    return portStayStartDateTime;
  }
}
