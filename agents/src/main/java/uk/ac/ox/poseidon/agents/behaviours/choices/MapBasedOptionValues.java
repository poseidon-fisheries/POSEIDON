/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.choices;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Map.Entry.comparingByValue;
import static one.util.streamex.MoreCollectors.maxAll;
import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;

public abstract class MapBasedOptionValues<O> implements OptionValues<O> {

    protected ImmutableList<Map.Entry<O, Double>> cachedBest = null;

    protected abstract Map<O, Double> getValues();

    @Override
    public Optional<Double> getValue(final O option) {
        return Optional.ofNullable(getValues().get(option));
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

    @Override
    public List<Map.Entry<O, Double>> getBestEntries() {
        if (cachedBest == null) {
            cachedBest = getValues()
                .entrySet()
                .stream()
                .collect(maxAll(comparingByValue(), toImmutableList()));
        }
        return cachedBest;
    }

    @Override
    public Optional<Map.Entry<O, Double>> getBestEntry(final MersenneTwisterFast rng) {
        final List<Map.Entry<O, Double>> bestEntries = getBestEntries();
        return bestEntries.isEmpty()
            ? Optional.empty()
            : Optional.of(oneOf(bestEntries, rng));
    }

}
