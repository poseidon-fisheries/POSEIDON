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

package uk.ac.ox.poseidon.core.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

/**
 * A {@link SimulationScopeFactory} that resolves the given steppable and registers it as a final
 * process on the simulation (run at {@code finish()}), then returns that same steppable. No
 * separate plain component class here: registering the final process is a side effect of building
 * the factory, not a distinct behavior class. Built via
 * {@link Factories#finalProcess(Factory)}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FinalProcessFactory<C extends Steppable> extends SimulationScopeFactory<C> {

    private Factory<? super SimulationScope, C> process;

    @Override
    protected C newInstance(final SimulationScope scope) {
        final C process = this.process.get(scope);
        scope.getSimulation().addFinalProcess(process);
        return process;
    }
}
