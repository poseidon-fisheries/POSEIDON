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

package uk.ac.ox.poseidon.core.predicates.numeric;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

/**
 * Represents a numeric predicate that checks if a given number falls within the range supplied by
 * {@code minimum} and {@code maximum} at test time, inclusive.
 * <p>
 * Since the bounds are read fresh on every {@link #test}, they may vary over time; unlike the
 * previous fixed-bound version, this class can no longer validate at construction time that
 * {@code maximum} is greater than {@code minimum}.
 */
@RequiredArgsConstructor
public class Between implements Predicate<Number> {

    private final @NonNull DoubleSupplier minimum;
    private final @NonNull DoubleSupplier maximum;

    @Override
    public boolean test(final Number number) {
        final double value = number.doubleValue();
        return value >= minimum.getAsDouble() && value <= maximum.getAsDouble();
    }
}
