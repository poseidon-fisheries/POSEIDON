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

package uk.ac.ox.poseidon.agents.vessels;

import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A scheduled {@link Steppable} that, when run, creates
 * {@link #numberOfVessels} vessels from a {@link VesselSpecification}, each
 * with a new id and with {@link #port} as its home port.
 */
@RequiredArgsConstructor
public class CreateVesselsAtPort implements Steppable {

  private final VesselSpecification specification;
  private final Supplier<String> vesselIds;
  private final Port port;
  private final int numberOfVessels;

  @Override
  public void step(final SimState simState) {
    for (int i = 0; i < numberOfVessels; i++) {
      specification.create(
        vesselIds.get(),
        Map.of(specification.getHomePortAttribute(), port.getCode())
      );
    }
  }
}
