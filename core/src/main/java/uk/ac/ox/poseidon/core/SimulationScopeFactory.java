/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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
 * A {@link Factory} whose produced object is kept in its {@link Simulation}'s
 * store — one instance per simulation run, shared by every resolution within
 * that run, but never shared across different runs.
 * <p>
 * Declare this level only when the product needs it: it holds mutable state of
 * its own, its build registers through the scope, or its build has side effects
 * on what its inputs give it. A product that only uses the simulation's
 * services (its random number generator, its clock, its event manager) takes
 * them as inputs of a {@link RelativeScopeFactory}, which is then
 * simulation-level anyway.
 */
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class SimulationScopeFactory<P> extends AbstractFactory<
  SimulationScope,
  P
> {

  @Override
  protected Class<SimulationScope> scopeClass() {
    return SimulationScope.class;
  }
}
