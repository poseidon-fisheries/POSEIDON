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
    // Tracked during the single visit, which runs for every option on each
    // pick, so the common case needs no further pass
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
    final int belowBest = entries.size() - best.size();
    return pickAmongTied(vessel, best).or(() ->
      belowBest > 0
        ? pickBelowBest(vessel, entries, values, belowBest)
        : Optional.empty()
    );
  }

  /**
   * Tests the groups of tied values below the best one, from the highest down,
   * stopping at the first group with an eligible option.
   *
   * @param belowBest the number of entries valued below the best value
   */
  private Optional<O> pickBelowBest(
    final Vessel vessel,
    final List<O> entries,
    final DoubleArrayList values,
    final int belowBest
  ) {
    final int[] order = IntStream.range(0, entries.size()).toArray();
    DoubleArrays.quickSortIndirect(order, values.elements(), 0, order.length);
    // Each element is the end, exclusive, of a group in ascending order
    return IntStream.iterate(
      belowBest,
      end -> end > 0,
      end -> groupStart(values, order, end)
    )
      .mapToObj(end -> pickInGroup(vessel, entries, values, order, end))
      .flatMap(Optional::stream)
      .findFirst();
  }

  /**
   * Tests the group ending just before {@code end} in {@code order}, taking its
   * entries from the highest index down.
   */
  private Optional<O> pickInGroup(
    final Vessel vessel,
    final List<O> entries,
    final DoubleArrayList values,
    final int[] order,
    final int end
  ) {
    final int start = groupStart(values, order, end);
    return pickAmongTied(
      vessel,
      IntStream.iterate(end - 1, i -> i >= start, i -> i - 1)
        .mapToObj(i -> entries.get(order[i]))
        .toList()
    );
  }

  /**
   * @return the start of the group of entries tied with the entry just before
   *         {@code end} in {@code order}
   */
  private static int groupStart(
    final DoubleArrayList values,
    final int[] order,
    final int end
  ) {
    final double groupValue = values.getDouble(order[end - 1]);
    return (
      IntStream.iterate(end - 2, i -> i >= 0, i -> i - 1)
        .filter(i -> values.getDouble(order[i]) != groupValue)
        .findFirst()
        .orElse(-1) + 1
    );
  }

  private Optional<O> pickAmongTied(final Vessel vessel, final List<O> tied) {
    return RandomPicker.pickFrom(vessel, tied, optionEligibilityCriteria, rng);
  }
}
