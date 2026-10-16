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

package uk.ac.ox.poseidon.core;

import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

/**
 * The scope that the build of a {@link SchedulingFactory} receives: a
 * {@link SimulationScope} that also offers the simulation's schedule. Only a
 * scheduling factory constructs one, and the inputs its build asks for receive
 * plain scopes, so only scheduling factories can schedule. A scheduled step
 * outlives the build that schedules it; the steps a scheduling factory
 * schedules belong to the simulation, which drops them all when it is dropped.
 */
public final class SchedulingScope extends SimulationScope {

  /**
   * @param simulationScope the scope of the simulation whose schedule this
   *                        scope offers
   */
  SchedulingScope(final SimulationScope simulationScope) {
    super(simulationScope);
  }

  /**
   * @return the simulation's schedule, on which the build puts its steps
   */
  public TemporalSchedule getSchedule() {
    return getSimulation().getTemporalSchedule();
  }
}
