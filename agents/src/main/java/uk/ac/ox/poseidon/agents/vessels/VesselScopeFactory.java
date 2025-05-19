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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.concurrent.ExecutionException;

public abstract class VesselScopeFactory<C> {

    // needs to be transient for SnakeYAML not to be confused
    // when there are no other properties to serialize
    private final transient Cache<Vessel, C> cache =
        CacheBuilder.newBuilder().weakValues().build();

    public final C get(
        final Simulation simulation,
        final Vessel vessel
    ) {
        try {
            return cache.get(vessel, () -> newInstance(simulation, vessel));
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract C newInstance(
        Simulation simulation,
        Vessel vessel
    );

}
