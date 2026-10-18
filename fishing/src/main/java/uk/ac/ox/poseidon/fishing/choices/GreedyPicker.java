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

package uk.ac.ox.poseidon.fishing.choices;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Comparator.comparingDouble;
import static java.util.Map.entry;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Picks, for a vessel, the option with the highest value among its option
 * values that passes a predicate for that vessel, breaking ties at random, or
 * gives {@code null} when no option passes (so that an
 * {@link EpsilonGreedyChooser} falls back to exploring).
 * <p>
 * Options are tested from the highest value down, one value at a time, and
 * testing stops at the first value with an option that passes: the predicate
 * can be costly (such as a check that computes a path), and is not called for
 * options that could not be picked anyway. Options tied at a value are tested
 * in a random order, stopping at the first that passes, which breaks the tie at
 * random among the passing options without testing all of them.
 * <p>
 * Infinite values are ordinary values, but a {@code NaN} value is an error: it
 * has no place in the order of values, so it would hide a broken valuation
 * behind an arbitrary pick.
 *
 * @param <O> the type of option
 */
public class GreedyPicker<O> implements Function<Vessel, O> {

  private final Function<
    ? super Vessel,
    ? extends OptionValues<O>
  > optionValues;
  private final BiFunction<? super Vessel, ? super O, Boolean> optionPredicate;
  private final MersenneTwisterFast rng;

  /**
   * @param optionValues    gives a vessel's values of the options to pick from
   * @param optionPredicate which options can be picked for a vessel
   * @param rng             breaks ties between options with the same value
   */
  GreedyPicker(
    final Function<? super Vessel, ? extends OptionValues<O>> optionValues,
    final BiFunction<? super Vessel, ? super O, Boolean> optionPredicate,
    final MersenneTwisterFast rng
  ) {
    this.optionValues = checkNotNull(optionValues);
    this.optionPredicate = checkNotNull(optionPredicate);
    this.rng = checkNotNull(rng);
  }

  /**
   * @param vessel the vessel to pick for
   * @return the picked option, or {@code null} if no option passes the
   *         predicate
   * @throws IllegalStateException if the value of any option is {@code NaN}
   */
  @Override
  public O apply(final Vessel vessel) {
    final List<Entry<O, Double>> entries = new ArrayList<>();
    optionValues.apply(vessel).forEachEntry((option, value) -> {
      checkState(!Double.isNaN(value), "Value of option %s is NaN.", option);
      entries.add(entry(option, value));
    });
    entries.sort(
      comparingDouble((Entry<O, Double> entry) -> entry.getValue()).reversed()
    );

    int start = 0;
    while (start < entries.size()) {
      final double value = entries.get(start).getValue();
      final List<O> tied = new ArrayList<>();
      int end = start;
      while (end < entries.size() && entries.get(end).getValue() == value) {
        tied.add(entries.get(end).getKey());
        end++;
      }
      // The first option to pass, in a random order, is a uniformly random pick
      // among the passing ones, without testing the others.
      final Optional<O> picked = shuffledStream(tied, rng)
        .filter(option -> optionPredicate.apply(vessel, option))
        .findFirst();
      if (picked.isPresent()) return picked.get();
      start = end;
    }
    return null;
  }
}
