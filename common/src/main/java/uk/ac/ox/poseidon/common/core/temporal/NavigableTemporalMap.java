/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.common.core.temporal;

import com.google.common.collect.ImmutableSortedMap;

import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class NavigableTemporalMap<K, V> implements TemporalMap<V> {
    private final NavigableMap<K, V> objects;
    private final Function<? super TemporalAccessor, ? extends K> temporalToKey;

    public NavigableTemporalMap(
        final Map<K, V> objects,
        final Function<? super TemporalAccessor, ? extends K> temporalToKey
    ) {
        this.objects = ImmutableSortedMap.copyOf(objects);
        this.temporalToKey = temporalToKey;
    }

    @Override
    public V get(final TemporalAccessor temporal) {
        final K key = temporalToKey.apply(temporal);
        final Map.Entry<K, V> entry = objects.floorEntry(key);
        checkNotNull(entry, "No object at or before " + key);
        return entry.getValue();
    }

}
