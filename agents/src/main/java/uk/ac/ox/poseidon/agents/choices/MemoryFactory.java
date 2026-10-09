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

package uk.ac.ox.poseidon.agents.choices;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

/**
 * A {@link SimulationScopeFactory} counterpart of {@link Memory}, built via
 * {@link Factories#memory()}.
 * <p>
 * Being simulation-scoped, the memory it builds is shared by the whole simulation. Wrap it in
 * {@link uk.ac.ox.poseidon.agents.vessels.Factories#perVessel} to give each vessel its own.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemoryFactory<O, M> extends SimulationScopeFactory<Memory<O, M>> {
    @Override
    protected Memory<O, M> newInstance(final SimulationScope scope) {
        return new Memory<>();
    }
}
