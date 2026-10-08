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
import it.unimi.dsi.fastutil.objects.ObjectDoubleBiConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;

/**
 * An {@link OptionValues} backed by a {@link Map} of option to value, computing (and caching)
 * the best entries by a full scan of that map.
 */
public abstract class MapBasedOptionValues<O> implements OptionValues<O> {

    /** Cached result of {@link #getBestEntries()}; cleared by subclasses on mutation. */
    protected ImmutableList<Map.Entry<O, Double>> cachedBest = null;

    /** @return the backing option-to-value map */
    protected abstract Map<O, Double> getValues();

    @Override
    public Optional<Double> getValue(final O option) {
        return Optional.ofNullable(getValues().get(option));
    }

    @Override
    public void forEachEntry(final ObjectDoubleBiConsumer<? super O> consumer) {
        getValues().forEach((option, value) -> consumer.accept(option, (double) value));
    }

    @Override
    public List<O> getBestOptions() {
        return getBestEntries().stream().map(Map.Entry::getKey).collect(toImmutableList());
    }

    @Override
    public Optional<O> getBestOption(final MersenneTwisterFast rng) {
        return getBestEntry(rng).map(Map.Entry::getKey);
    }

    @Override
    public Optional<Double> getBestValue() {
        return getBestEntries().stream().findAny().map(Map.Entry::getValue);
    }

    /** @return every entry tied for the highest value, computed by a full scan and cached in {@link #cachedBest} */
    @Override
    public List<Map.Entry<O, Double>> getBestEntries() {
        if (cachedBest == null) {
            final List<Map.Entry<O, Double>> best = new ArrayList<>();
            double bestValue = Double.NEGATIVE_INFINITY;
            for (final Map.Entry<O, Double> entry : getValues().entrySet()) {
                final double v = entry.getValue();
                if (v > bestValue) {
                    bestValue = v;
                    best.clear();
                    best.add(entry);
                } else if (v == bestValue) {
                    best.add(entry);
                }
            }
            cachedBest = ImmutableList.copyOf(best);
        }
        return cachedBest;
    }

    /** @param rng the RNG to break ties with; @return one entry uniformly picked among the best, if any */
    @Override
    public Optional<Map.Entry<O, Double>> getBestEntry(final MersenneTwisterFast rng) {
        final List<Map.Entry<O, Double>> bestEntries = getBestEntries();
        return bestEntries.isEmpty()
            ? Optional.empty()
            : Optional.of(oneOf(bestEntries, rng));
    }

}
