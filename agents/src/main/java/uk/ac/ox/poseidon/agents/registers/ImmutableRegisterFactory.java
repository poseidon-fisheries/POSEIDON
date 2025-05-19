/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.registers;

import lombok.*;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

import java.util.List;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImmutableRegisterFactory<T> extends SimulationScopeFactory<Register<T>> {

    private Factory<? extends List<Vessel>> vessels;
    private VesselScopeFactory<T> vesselScopeFactory;

    @Override
    protected ImmutableRegister<T> newInstance(final @NonNull Simulation simulation) {
        return new ImmutableRegister<>(
            vessels.get(simulation).stream().collect(toImmutableMap(
                identity(),
                vessel -> vesselScopeFactory.get(simulation, vessel)
            ))
        );
    }

}
