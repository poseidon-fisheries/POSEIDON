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

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import java.util.List;
import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.Value;

/**
 * A mapping that gives the label of the interval a number falls in: the number
 * is read from the thing mapped, under the context, and placed among
 * non-overlapping intervals. Bounds are lower-inclusive and upper-exclusive;
 * null bounds are unbounded. Built via
 * {@link Factories Factories.intervalLabel(...)} in this package, with labels
 * that are strings.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 * @param <R> the type of the labels
 */
public class IntervalLabel<C, T, R> implements BiFunction<C, T, R> {

  private final BiFunction<? super C, ? super T, ? extends Double> number;
  private final List<Interval<R>> intervals;

  /**
   * @param number    the mapping that reads the number to place
   * @param intervals the non-overlapping intervals to place it among; each
   *                  must have at least one bound, and a lower bound strictly
   *                  less than its upper bound
   */
  public IntervalLabel(
    @NonNull final BiFunction<? super C, ? super T, ? extends Double> number,
    final List<Interval<R>> intervals
  ) {
    this.number = number;
    this.intervals = List.copyOf(intervals);
    validateIntervals(this.intervals);
  }

  /**
   * @return the label of the interval containing the number, or {@code null} if
   *         no interval contains it
   * @throws NullPointerException if the number is {@code null}
   */
  @Override
  public R apply(final C context, final T t) {
    final double value = number.apply(context, t);
    // A loop, not a stream: this runs for every cell valued, and the stream's
    // overhead showed in profiles.
    for (final Interval<R> interval : intervals) {
      if (interval.contains(value)) return interval.getMappedValue();
    }
    return null;
  }

  private static void validateIntervals(
    final List<? extends Interval<?>> intervals
  ) {
    intervals.forEach(IntervalLabel::validateBounds);
    final List<? extends Interval<?>> sortedIntervals = intervals
      .stream()
      .sorted(
        comparing(
          Interval::getLowerBoundInclusive,
          nullsFirst(Double::compareTo)
        )
      )
      .toList();
    for (int i = 1; i < sortedIntervals.size(); i++) {
      final Interval<?> previous = sortedIntervals.get(i - 1);
      final Interval<?> current = sortedIntervals.get(i);
      if (overlaps(previous, current)) {
        throw new IllegalArgumentException("Intervals overlap.");
      }
    }
  }

  private static void validateBounds(final Interval<?> interval) {
    if (
      interval.lowerBoundInclusive == null &&
      interval.upperBoundExclusive == null
    ) {
      throw new IllegalArgumentException(
        "Interval must have at least one bound."
      );
    }
    if (
      interval.lowerBoundInclusive != null &&
      interval.upperBoundExclusive != null &&
      interval.lowerBoundInclusive >= interval.upperBoundExclusive
    ) {
      throw new IllegalArgumentException(
        "Interval lower bound must be strictly less than upper bound."
      );
    }
  }

  private static boolean overlaps(
    final Interval<?> previous,
    final Interval<?> current
  ) {
    return (
      previous.upperBoundExclusive == null ||
      current.lowerBoundInclusive == null ||
      current.lowerBoundInclusive < previous.upperBoundExclusive
    );
  }

  /**
   * An interval and its label.
   *
   * @param <R> the type of the label
   */
  @Value
  public static class Interval<R> {

    // Null means the interval is unbounded on that side.
    Double lowerBoundInclusive;
    Double upperBoundExclusive;
    R mappedValue;

    /** @return whether {@code value} falls within this interval's bounds */
    public boolean contains(final double value) {
      return lowerBoundContains(value) && upperBoundContains(value);
    }

    private boolean lowerBoundContains(final double value) {
      return lowerBoundInclusive == null || lowerBoundInclusive <= value;
    }

    private boolean upperBoundContains(final double value) {
      return upperBoundExclusive == null || value < upperBoundExclusive;
    }
  }
}
