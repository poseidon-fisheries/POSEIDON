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

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableMap;

/**
 * Specifications for tests that construct vessels themselves rather than
 * creating them through a specification.
 */
final class SpecificationFixtures {

  /** The attribute giving a vessel's home port, in these specifications. */
  static final String HOME_PORT = "home_port";

  private SpecificationFixtures() {}

  /**
   * @return a specification of vessels with {@code behaviour} and no other
   *         component, whose home port is given by {@link #HOME_PORT}; it has
   *         no fleet, so it creates no vessel
   */
  static VesselSpecification specificationOf(
    final VesselComponent<? extends Task<Vessel>> behaviour
  ) {
    return new VesselSpecification(
      null,
      null,
      behaviour,
      ImmutableMap.of(),
      HOME_PORT,
      null
    );
  }
}
