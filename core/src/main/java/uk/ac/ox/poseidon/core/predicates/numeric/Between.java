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

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a numeric predicate that checks if a given number falls within a specified range
 * [minimum, maximum], inclusive.
 * <p>
 * Note that the {@code maximum} value must always be greater than the {@code minimum} value. If
 * this condition is not met, an {@code IllegalArgumentException} will be thrown during
 * construction.
 */
public class Between implements Predicate<Number> {

    private final double minimum;
    private final double maximum;

    public Between(
        final double minimum,
        final double maximum
    ) {
        checkArgument(
            maximum > minimum,
            "Maximum (%s) must be greater than minimum (%s)".formatted(maximum, minimum)
        );
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public boolean test(final Number number) {
        final double value = number.doubleValue();
        return value >= minimum && value <= maximum;
    }
}
