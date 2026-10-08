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

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PACKAGE;

/**
 * A separate {@link Memory} for each key, such as the code of the gear the observations were made
 * with, so that observations made in one context are only read back in the same context. Which key
 * an observation is remembered under, and which key a reader looks up, is up to the caller.
 *
 * @param <K> the type of key
 * @param <O> the type of option observed
 * @param <M> the type of what is remembered about an option
 */
@NoArgsConstructor(access = PACKAGE)
public class KeyedMemory<K, O, M> {

    private final Map<K, Memory<O, M>> memories = new HashMap<>();

    /**
     * Gives the memory for {@code key}, to remember observations in or to read from. A key not
     * seen before gets a new, empty memory, which is kept from then on.
     *
     * @param key the key to look up; may be {@code null}
     * @return the memory for {@code key}, always the same one for the same key
     */
    public Memory<O, M> get(final K key) {
        return memories.computeIfAbsent(key, k -> new Memory<>());
    }
}
