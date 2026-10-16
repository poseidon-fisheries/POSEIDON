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

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A simulation-level {@link Factory} whose build schedules steps: core's
 * scheduling wrappers and the fleet. Its product is kept in its simulation's
 * store, as any simulation-level product is, and its build receives a
 * {@link SchedulingScope}, which offers the simulation's schedule. The inputs
 * the build asks for receive plain scopes. What it schedules needs no undo:
 * the simulation's store ends only when the simulation finishes.
 */
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class SchedulingFactory<P> extends SimulationScopeFactory<P> {

  /**
   * Builds the product with {@link #newInstance(SchedulingScope)}, under a
   * scheduling scope over {@code scope}.
   */
  @Override
  protected final P newInstance(final SimulationScope scope) {
    return newInstance(new SchedulingScope(scope));
  }

  /**
   * @param scope the scope being resolved against, which offers the
   *              simulation's schedule
   * @return a freshly built {@code P}, called at most once per simulation
   */
  protected abstract P newInstance(SchedulingScope scope);
}
