/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.scopes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ox.poseidon.core.Simulation;

import java.lang.ref.WeakReference;

/**
 * A {@link Scope} tied to a single {@link Simulation} run, used by factories whose output must
 * not be shared across simulations (see {@code SimulationScopeFactory}). Holds the simulation via
 * a {@link WeakReference} so a scope kept alive elsewhere doesn't itself keep a finished
 * simulation from being garbage-collected.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimulationScope extends Scope {

    private final WeakReference<Simulation> simulation;

    /**
     * Copy constructor: shares the same underlying simulation reference as {@code simulationScope}.
     */
    public SimulationScope(final SimulationScope simulationScope) {
        this.simulation = simulationScope.simulation;
    }

    /**
     * @param simulation the simulation this scope is tied to, held via a {@link WeakReference}
     */
    public SimulationScope(
        final Simulation simulation
    ) {
        this.simulation = new WeakReference<>(simulation);
    }

    /**
     * @return the scoped simulation, or {@code null} if it has already been garbage-collected.
     */
    public Simulation getSimulation() {
        return simulation.get();
    }

}
