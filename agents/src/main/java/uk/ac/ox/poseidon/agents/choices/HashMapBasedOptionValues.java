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
import ec.util.MersenneTwisterFast;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectDoubleBiConsumer;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;

@Getter
@NoArgsConstructor(access = PACKAGE)
public abstract class HashMapBasedOptionValues<O>
    extends MapBasedOptionValues<O>
    implements MutableOptionValues<O> {

    protected final Object2DoubleOpenHashMap<O> values = new Object2DoubleOpenHashMap<>();
    private ImmutableList<O> cachedBestKeys = null;
    private double cachedBestValue = Double.NEGATIVE_INFINITY;

    @Override
    public void observe(
        final O option,
        final double value
    ) {
        final double oldValue = values.getOrDefault(option, 0.0);
        final double newValue = newValue(option, oldValue, value);
        values.put(option, newValue);
        updateBestCache(option, newValue);
    }

    private void updateBestCache(final O option, final double newValue) {
        if (cachedBestKeys == null) return;
        if (newValue > cachedBestValue) {
            cachedBestKeys = ImmutableList.of(option);
            cachedBestValue = newValue;
        } else if (newValue == cachedBestValue) {
            if (!cachedBestKeys.contains(option)) {
                cachedBestKeys = ImmutableList.<O>builder()
                    .addAll(cachedBestKeys)
                    .add(option)
                    .build();
            }
        } else if (cachedBestKeys.contains(option)) {
            if (cachedBestKeys.size() == 1) {
                cachedBestKeys = null;
            } else {
                cachedBestKeys = cachedBestKeys.stream()
                    .filter(k -> !k.equals(option))
                    .collect(toImmutableList());
            }
        }
    }

    private void computeBestCache() {
        ImmutableList.Builder<O> bestKeys = ImmutableList.builder();
        double bestValue = Double.NEGATIVE_INFINITY;
        final ObjectIterator<Object2DoubleMap.Entry<O>> iterator =
            values.object2DoubleEntrySet().fastIterator();
        while (iterator.hasNext()) {
            final Object2DoubleMap.Entry<O> entry = iterator.next();
            final double v = entry.getDoubleValue();
            if (v > bestValue) {
                bestValue = v;
                bestKeys = ImmutableList.builder();
                bestKeys.add(entry.getKey());
            } else if (v == bestValue) {
                bestKeys.add(entry.getKey());
            }
        }
        cachedBestKeys = bestKeys.build();
        cachedBestValue = bestValue;
    }

    @Override
    public void forEachBestEntry(final ObjectDoubleBiConsumer<? super O> consumer) {
        if (cachedBestKeys == null) computeBestCache();
        for (int i = 0; i < cachedBestKeys.size(); i++) {
            consumer.accept(cachedBestKeys.get(i), cachedBestValue);
        }
    }

    @Override
    public List<Map.Entry<O, Double>> getBestEntries() {
        if (cachedBestKeys == null) computeBestCache();
        return cachedBestKeys.stream()
            .map(k -> new AbstractMap.SimpleEntry<>(k, cachedBestValue))
            .collect(toImmutableList());
    }

    @Override
    public List<O> getBestOptions() {
        if (cachedBestKeys == null) computeBestCache();
        return cachedBestKeys;
    }

    @Override
    public Optional<Double> getBestValue() {
        if (cachedBestKeys == null) computeBestCache();
        if (cachedBestKeys.isEmpty()) return Optional.empty();
        return Optional.of(cachedBestValue);
    }

    @Override
    public Optional<Map.Entry<O, Double>> getBestEntry(final MersenneTwisterFast rng) {
        if (cachedBestKeys == null) computeBestCache();
        if (cachedBestKeys.isEmpty()) return Optional.empty();
        final O key = oneOf(cachedBestKeys, rng);
        return Optional.of(new AbstractMap.SimpleEntry<>(key, cachedBestValue));
    }

    protected void invalidateCache() {
        cachedBestKeys = null;
    }

    protected abstract double newValue(
        O option,
        double oldValue,
        double observedValue
    );
}
