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

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PACKAGE;

@Getter
@NoArgsConstructor(access = PACKAGE)
public abstract class HashMapBasedOptionValues<O>
    extends MapBasedOptionValues<O>
    implements MutableOptionValues<O> {

    protected final Object2DoubleOpenHashMap<O> values = new Object2DoubleOpenHashMap<>();

    @Override
    public void observe(
        final O option,
        final double value
    ) {
        final double oldValue = values.getOrDefault(option, 0.0);
        values.put(option, newValue(option, oldValue, value));
        invalidateCache();
    }

    @Override
    public List<Map.Entry<O, Double>> getBestEntries() {
        if (cachedBest == null) {
            final List<Map.Entry<O, Double>> best = new ArrayList<>();
            double bestValue = Double.NEGATIVE_INFINITY;
            final ObjectIterator<Object2DoubleMap.Entry<O>> iterator =
                values.object2DoubleEntrySet().fastIterator();
            while (iterator.hasNext()) {
                final Object2DoubleMap.Entry<O> entry = iterator.next();
                final double v = entry.getDoubleValue();
                if (v > bestValue) {
                    bestValue = v;
                    best.clear();
                    best.add(new AbstractMap.SimpleEntry<>(entry.getKey(), v));
                } else if (v == bestValue) {
                    best.add(new AbstractMap.SimpleEntry<>(entry.getKey(), v));
                }
            }
            cachedBest = ImmutableList.copyOf(best);
        }
        return cachedBest;
    }

    protected void invalidateCache() {
        cachedBest = null;
    }

    protected abstract double newValue(
        O option,
        double oldValue,
        double observedValue
    );
}
