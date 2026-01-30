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

package uk.ac.ox.poseidon.agents.components;

import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Stores per-vessel components for a simulation so other systems can query them by vessel.
 */
public class ComponentRegister<C> {

    private final Map<Vessel, C> map = new HashMap<>();

    /**
     * Associates the given component with the vessel.
     */
    public void putComponent(
        final Vessel vessel,
        final C component
    ) {
        map.put(vessel, component);
    }

    /**
     * Retrieves the component for the vessel, if available.
     */
    public Optional<C> getComponent(final Vessel vessel) {
        return Optional.ofNullable(map.get(vessel));
    }

    /**
     * Returns a stream of vessels currently in the register.
     */
    public Stream<Vessel> getVessels() {
        return map.keySet().stream();
    }

    /**
     * Returns a stream of all vessel/component entries.
     */
    public Stream<Map.Entry<Vessel, C>> getAllEntries() {
        return map.entrySet().stream();
    }

    /**
     * Returns a stream of entries excluding the provided vessel.
     */
    public Stream<Entry<Vessel, C>> getOtherEntries(final Vessel vessel) {
        return getAllEntries().filter(entry -> !entry.getKey().equals(vessel));
    }

}
