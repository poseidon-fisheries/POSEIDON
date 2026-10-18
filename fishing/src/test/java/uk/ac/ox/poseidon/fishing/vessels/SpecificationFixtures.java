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

package uk.ac.ox.poseidon.fishing.vessels;

import static org.mockito.Mockito.mock;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * Specifications for tests that have a fleet construct vessels rather than
 * creating them through a specification, and the vessels constructed so.
 */
final class SpecificationFixtures {

  /** The attribute giving a vessel's home port, in these specifications. */
  static final String HOME_PORT = "home_port";

  private SpecificationFixtures() {}

  /**
   * @return a specification of vessels with {@code behaviour} and the other
   *         {@code components}, whose home port is given by {@link #HOME_PORT},
   *         building over a simulation scope of a mocked simulation; it has no
   *         fleet, so it creates no vessel
   */
  static VesselSpecification specificationOf(
    final VesselComponent<? extends Task<Vessel>> behaviour,
    final VesselComponent<?>... components
  ) {
    final ImmutableMap.Builder<String, VesselComponent<?>> componentsByName =
      ImmutableMap.builder();
    for (int i = 0; i < components.length; i++) {
      componentsByName.put("component" + i, components[i]);
    }
    return new VesselSpecification(
      new SimulationScope(mock(Simulation.class)),
      null,
      behaviour,
      componentsByName.build(),
      HOME_PORT,
      null
    );
  }

  /**
   * @return a vessel {@code "vessel"} constructed by a fleet over
   *         {@code schedule}, {@code vesselField} and {@code portGrid}, with
   *         an account, {@code behaviour} and the other {@code components},
   *         but no attribute and no component built yet
   */
  static Vessel newVessel(
    final TemporalSchedule schedule,
    final VesselField vesselField,
    final PortGrid portGrid,
    final VesselComponent<? extends Task<Vessel>> behaviour,
    final VesselComponent<?>... components
  ) {
    return new Fleet(
      schedule,
      new SimpleEventManager(),
      vesselField,
      portGrid,
      mock(MarketGrid.class)
    ).createVessel("vessel", specificationOf(behaviour, components));
  }
}
