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

package uk.ac.ox.poseidon.agents.vessels;

import uk.ac.ox.poseidon.core.AgentScopeFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.lang.reflect.InvocationTargetException;

public abstract class VesselScopeFactory<C> extends AgentScopeFactory<Vessel, C> {

    @Override
    protected Integer makeKey(
        final Simulation simulation,
        final Vessel vessel
    ) {
        synchronized (this) {
            return readMethods
                .stream()
                .map(readMethod -> {
                    try {
                        return readMethod.invoke(this);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(o ->
                    switch (o) {
                        case null -> null;
                        case final Factory<?> factory -> factory.get(simulation);
                        case final VesselScopeFactory<?> factory -> factory.get(simulation, vessel);
                        default -> o;
                    }
                )
                .toList()
                .hashCode();
        }
    }

}
