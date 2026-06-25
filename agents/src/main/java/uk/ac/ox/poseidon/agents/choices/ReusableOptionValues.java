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

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectDoubleBiConsumer;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Map;

/**
 * A reusable {@link OptionValues} implementation backed by a single
 * {@link Object2DoubleOpenHashMap}. After populating the map and
 * consuming the result, call {@link #clear()} to reset for re-use.
 * <p>
 * This instance must not be accessed concurrently or while another
 * caller holds a reference returned by a prior call (the returned
 * reference IS this instance).
 */
class ReusableOptionValues<O> extends MapBasedOptionValues<O> {

    private final Object2DoubleOpenHashMap<O> values = new Object2DoubleOpenHashMap<>();

    void clear() {
        values.clear();
        cachedBest = null;
    }

    void putIfGreater(final O key, final double value) {
        if (value > values.getOrDefault(key, Double.NEGATIVE_INFINITY)) {
            values.put(key, value);
        }
    }

    @Override
    public void forEachBestEntry(final ObjectDoubleBiConsumer<? super O> consumer) {
        double bestValue = Double.NEGATIVE_INFINITY;
        final ObjectIterator<Object2DoubleMap.Entry<O>> iterator =
            values.object2DoubleEntrySet().fastIterator();
        while (iterator.hasNext()) {
            final Object2DoubleMap.Entry<O> entry = iterator.next();
            final double v = entry.getDoubleValue();
            if (v > bestValue) {
                bestValue = v;
                consumer.accept(entry.getKey(), v);
            } else if (v == bestValue) {
                consumer.accept(entry.getKey(), v);
            }
        }
    }

    @Override
    protected Map<O, Double> getValues() {
        return values;
    }
}
