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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.core.providers.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparingDouble;
import static java.util.Map.entry;
import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;

/**
 * Picks the option with the highest value that passes a predicate, breaking ties at random, or
 * gives {@code null} when no option passes (so that an
 * {@link EpsilonGreedyChooser} falls back to exploring).
 * <p>
 * Options are tested from the highest value down, one value at a time, and testing stops at the
 * first value with an option that passes: the predicate can be costly (such as a check that
 * computes a path), and is not called for options that could not be picked anyway.
 *
 * @param <O> the type of option
 */
public class GreedyPicker<O> implements Provider<O> {

    private final OptionValues<O> optionValues;
    private final Predicate<? super O> optionPredicate;
    private final MersenneTwisterFast rng;

    /**
     * @param optionValues    the values of the options to pick from
     * @param optionPredicate which options can be picked
     * @param rng             breaks ties between options with the same value
     */
    GreedyPicker(
        final OptionValues<O> optionValues,
        final Predicate<? super O> optionPredicate,
        final MersenneTwisterFast rng
    ) {
        this.optionValues = checkNotNull(optionValues);
        this.optionPredicate = checkNotNull(optionPredicate);
        this.rng = checkNotNull(rng);
    }

    @Override
    public O get() {
        final List<Entry<O, Double>> entries = new ArrayList<>();
        optionValues.forEachEntry((option, value) -> entries.add(entry(option, value)));
        entries.sort(comparingDouble((Entry<O, Double> entry) -> entry.getValue()).reversed());

        int start = 0;
        while (start < entries.size()) {
            final double value = entries.get(start).getValue();
            final List<O> passing = new ArrayList<>();
            int end = start;
            while (end < entries.size() && entries.get(end).getValue() == value) {
                final O option = entries.get(end).getKey();
                if (optionPredicate.test(option)) passing.add(option);
                end++;
            }
            if (!passing.isEmpty()) return oneOf(passing, rng);
            start = end;
        }
        return null;
    }
}
