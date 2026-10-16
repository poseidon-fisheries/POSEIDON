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
 * that run, but never shared across different runs. Use for components that
 * depend on a live simulation's state (its RNG, its
 * {@link uk.ac.ox.poseidon.core.schedule.TemporalSchedule}, etc.).
 */
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class SimulationScopeFactory<P> extends AbstractFactory<
  SimulationScope,
  P
> {

  @Override
  protected ProductStore productStoreFor(final SimulationScope scope) {
    return scope.getSimulation().getProductStore();
  }

  /**
   * Gives the build a plain {@link SimulationScope}, whatever narrower scope
   * the product is asked for with, so that what a narrower scope offers, such
   * as a vessel or the simulation's schedule, never reaches a
   * simulation-level build.
   */
  @Override
  protected SimulationScope scopeForBuild(final SimulationScope scope) {
    return scope.getClass() == SimulationScope.class
      ? scope
      : new SimulationScope(scope);
  }

  @Override
  protected Class<SimulationScope> scopeClass() {
    return SimulationScope.class;
  }
}
