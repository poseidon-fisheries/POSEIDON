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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * Holds for a vessel docked at the port {@code port} returns for it, read
 * fresh on each call; does not hold for a vessel at sea.
 */
@RequiredArgsConstructor
public class VesselIsDockedAt implements Function<Vessel, Boolean> {

  private final Function<? super Vessel, Port> port;

  /**
   * @throws NullPointerException if {@code port} returns {@code null}
   */
  @Override
  public Boolean apply(final Vessel vessel) {
    final Port port = checkNotNull(
      this.port.apply(vessel),
      "No port to test vessel %s against.",
      vessel.getId()
    );
    return port.equals(vessel.getPortDockedAt());
  }
}
