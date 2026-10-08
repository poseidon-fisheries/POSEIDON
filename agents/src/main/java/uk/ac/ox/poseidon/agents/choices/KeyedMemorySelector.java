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

package uk.ac.ox.poseidon.agents.choices;

import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import static lombok.AccessLevel.PACKAGE;

/**
 * Selects, from a {@link KeyedMemory}, the {@link Memory} for the key extracted from its input,
 * such as the code of the gear used in a {@code FishingEvent} when remembering a haul, or of the
 * gear a vessel currently uses when reading. Several selectors can share one keyed memory, each
 * extracting the key from a different kind of input.
 *
 * @param <T> the type of input the key is extracted from
 * @param <K> the type of key
 * @param <O> the type of option observed
 * @param <M> the type of what is remembered about an option
 */
@RequiredArgsConstructor(access = PACKAGE)
public class KeyedMemorySelector<T, K, O, M> implements Function<T, Memory<O, M>> {

    private final KeyedMemory<K, O, M> keyedMemory;
    private final Function<? super T, ? extends K> keyExtractor;

    @Override
    public Memory<O, M> apply(final T input) {
        return keyedMemory.get(keyExtractor.apply(input));
    }
}
