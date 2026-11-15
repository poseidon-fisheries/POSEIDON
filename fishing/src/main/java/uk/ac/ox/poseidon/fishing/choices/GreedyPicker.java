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

import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.PACKAGE;

import ec.util.MersenneTwisterFast;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Visits a vessel's option values once, then picks the highest-valued eligible
 * option. Ties are tested in random order, stopping at the first eligible
 * option. Lower values are considered only when every better option fails.
 * Values use primitive storage and sorting is deferred until the best options
 * all fail eligibility. Infinite values are allowed; NaN is an error.
 * @param <O> the option type
 */
@RequiredArgsConstructor(access = PACKAGE)
public class GreedyPicker<O> implements Function<Vessel, Optional<O>> {

  private final @NonNull OptionValues<O> optionValues;
  private final @NonNull BiFunction<
    ? super Vessel,
    ? super O,
    Boolean
  > optionEligibilityCriteria;
  private final @NonNull MersenneTwisterFast rng;

  @Override
  public Optional<O> apply(final Vessel vessel) {
    final ObjectArrayList<O> entries = new ObjectArrayList<>();
    final DoubleArrayList values = new DoubleArrayList();
    final ObjectArrayList<O> best = new ObjectArrayList<>();
    final double[] bestValue = { Double.NEGATIVE_INFINITY };
    optionValues.forEachEntry(vessel, (option, value) -> {
      checkState(!Double.isNaN(value), "Value of option %s is NaN.", option);
      entries.add(option);
      values.add(value);
      if (value > bestValue[0]) {
        bestValue[0] = value;
        best.clear();
      }
      if (value == bestValue[0]) {
        best.add(option);
      }
    });
    Optional<O> result = pickAmongTied(vessel, best);
    if (result.isEmpty() && best.size() < entries.size()) {
      final int[] order = IntStream.range(0, entries.size()).toArray();
      DoubleArrays.quickSortIndirect(order, values.elements(), 0, order.length);
      int end = entries.size() - best.size();
      while (result.isEmpty() && end > 0) {
        final double groupValue = values.getDouble(order[end - 1]);
        final ObjectArrayList<O> tied = new ObjectArrayList<>();
        do {
          end--;
          tied.add(entries.get(order[end]));
        } while (end > 0 && values.getDouble(order[end - 1]) == groupValue);
        result = pickAmongTied(vessel, tied);
      }
    }
    return result;
  }

  private Optional<O> pickAmongTied(final Vessel vessel, final List<O> tied) {
    return RandomPicker.pickFrom(vessel, tied, optionEligibilityCriteria, rng);
  }
}
