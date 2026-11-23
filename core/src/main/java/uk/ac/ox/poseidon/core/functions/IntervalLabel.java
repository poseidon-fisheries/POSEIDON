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

package uk.ac.ox.poseidon.core.functions;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.function.BiFunction;
import lombok.NonNull;

/**
 * A mapping that gives the label of the interval a number falls in: the number
 * is read from the thing mapped, under the context, and placed among intervals
 * given by their lower bounds. An interval runs from its lower bound, inclusive,
 * up to the next lower bound, exclusive, and the last interval has no upper
 * bound. A number below the first lower bound has no label. Built via
 * {@link Factories Factories.intervalLabel(...)} in this package, with labels
 * that are strings.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 * @param <R> the type of the labels
 */
public class IntervalLabel<C, T, R> implements BiFunction<C, T, R> {

  private final BiFunction<? super C, ? super T, ? extends Double> number;
  // Arrays, not a map: this runs for every cell valued, and boxing showed in
  // profiles.
  private final double[] lowerBounds;
  private final List<R> labels;

  /**
   * @param number the mapping that reads the number to place
   * @param labelsByLowerBound each interval's label, by its lower bound, in
   *               ascending order of bound
   */
  public IntervalLabel(
    @NonNull final BiFunction<? super C, ? super T, ? extends Double> number,
    @NonNull final SortedMap<Double, R> labelsByLowerBound
  ) {
    checkArgument(
      !labelsByLowerBound.isEmpty(),
      "Interval labels need at least one interval"
    );
    labelsByLowerBound
      .keySet()
      .forEach(bound ->
        checkArgument(!bound.isNaN(), "An interval's lower bound is NaN")
      );
    this.number = number;
    lowerBounds = labelsByLowerBound
      .keySet()
      .stream()
      .mapToDouble(Double::doubleValue)
      .toArray();
    labels = new ArrayList<>(labelsByLowerBound.values());
  }

  /**
   * @return the label of the interval containing the number, or {@code null} if
   *         the number is below the first lower bound or is {@code NaN}
   * @throws NullPointerException if the number is {@code null}
   */
  @Override
  public R apply(final C context, final T t) {
    final double value = number.apply(context, t);
    // Adding 0.0 turns -0.0 into 0.0, which the search would place below it
    final int found = Arrays.binarySearch(lowerBounds, value + 0.0);
    final int index = found >= 0 ? found : -found - 2;
    return Double.isNaN(value) || index < 0 ? null : labels.get(index);
  }
}
