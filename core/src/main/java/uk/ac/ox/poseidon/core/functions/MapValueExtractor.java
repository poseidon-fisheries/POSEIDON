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

package uk.ac.ox.poseidon.core.functions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

/**
 * A {@link Function} that looks up its input in a fixed, resolved {@link Map} on every call.
 * Unlike {@link MapEntryFactory}, the key is supplied per-call rather than fixed at resolution
 * time. Built via {@link Factories#mapValueExtractor(uk.ac.ox.poseidon.core.Factory)} in this
 * package.
 */
@RequiredArgsConstructor
public class MapValueExtractor<K, V> implements Function<K, V> {

    @NonNull private final Map<? super K, ? extends V> map;

    @Override
    public V apply(final K k) {
        return map.get(k);
    }

}
