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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

    // A field rather than a local so its backing array survives across forEachBestEntry() calls
    // (clear() resets size to 0 but keeps the array) instead of being reallocated every call.
    // Holds only the keys currently tied for the best value, not the whole map.
    private final ObjectArrayList<O> bestKeysBuffer = new ObjectArrayList<>();

    /** Empties this instance so it can be reused for a fresh round of {@link #putIfGreater} calls. */
    void clear() {
        values.clear();
        cachedBest = null;
    }

    /** Sets {@code key}'s value to {@code value} only if it's greater than what's currently stored. */
    void putIfGreater(final O key, final double value) {
        if (value > values.getOrDefault(key, Double.NEGATIVE_INFINITY)) {
            values.put(key, value);
        }
    }

    /**
     * Calls {@code consumer} once per entry tied for the highest value, in a single pass:
     * {@link #bestKeysBuffer} collects the keys tied for the best value seen so far, is cleared
     * whenever a strictly higher value is found, and is only iterated (not the whole map) once
     * the best value is settled.
     */
    @Override
    public void forEachBestEntry(final ObjectDoubleBiConsumer<? super O> consumer) {
        double bestValue = Double.NEGATIVE_INFINITY;
        bestKeysBuffer.clear();
        final ObjectIterator<Object2DoubleMap.Entry<O>> iterator =
            values.object2DoubleEntrySet().fastIterator();
        while (iterator.hasNext()) {
            final Object2DoubleMap.Entry<O> entry = iterator.next();
            final double v = entry.getDoubleValue();
            if (v > bestValue) {
                bestValue = v;
                bestKeysBuffer.clear();
                bestKeysBuffer.add(entry.getKey());
            } else if (v == bestValue) {
                bestKeysBuffer.add(entry.getKey());
            }
        }
        for (final O key : bestKeysBuffer) {
            consumer.accept(key, bestValue);
        }
    }

    @Override
    protected Map<O, Double> getValues() {
        return values;
    }
}
