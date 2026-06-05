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

import lombok.Value;

import java.util.List;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

/**
 * Maps numeric values to labels using non-overlapping intervals. Bounds are lower-inclusive and
 * upper-exclusive; null bounds are unbounded.
 */
@Value
public class NumericIntervalMapper<T> implements Function<Double, T> {

    List<Interval<T>> intervals;

    public NumericIntervalMapper(final List<Interval<T>> intervals) {
        this.intervals = List.copyOf(intervals);
        validateIntervals(this.intervals);
    }

    @Override
    public T apply(final Double value) {
        return intervals.stream()
            .filter(interval -> interval.contains(value))
            .map(Interval::getMappedValue)
            .findFirst()
            .orElse(null);
    }

    private static void validateIntervals(final List<? extends Interval<?>> intervals) {
        intervals.forEach(NumericIntervalMapper::validateBounds);
        final List<? extends Interval<?>> sortedIntervals =
            intervals.stream()
                .sorted(comparing(Interval::getLowerBoundInclusive, nullsFirst(Double::compareTo)))
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
        if (interval.lowerBoundInclusive == null && interval.upperBoundExclusive == null) {
            throw new IllegalArgumentException("Interval must have at least one bound.");
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
        return previous.upperBoundExclusive == null ||
            current.lowerBoundInclusive == null ||
            current.lowerBoundInclusive < previous.upperBoundExclusive;
    }

    @Value
    public static class Interval<T> {

        // Null means the interval is unbounded on that side.
        Double lowerBoundInclusive;
        Double upperBoundExclusive;
        T mappedValue;

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
