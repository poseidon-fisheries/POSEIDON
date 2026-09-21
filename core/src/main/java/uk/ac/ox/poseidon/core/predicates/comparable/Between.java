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

package uk.ac.ox.poseidon.core.predicates.comparable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A {@link Predicate} that is true iff the tested value lies between a minimum and a maximum
 * (each supplied, and each independently inclusive or exclusive). Built via
 * {@link Factories Factories.between(...)} in this package.
 *
 * @param <T> the {@link Comparable} type of the bounds
 */
@RequiredArgsConstructor
public class Between<T> implements Predicate<Comparable<T>> {

    private final @NonNull Supplier<? extends T> minimum;
    private final @NonNull Supplier<? extends T> maximum;
    private final boolean includeMinimum;
    private final boolean includeMaximum;

    @Override
    public boolean test(final @NonNull Comparable<T> other) {
        final int comparedToMinimum = other.compareTo(minimum.get());
        final int comparedToMaximum = other.compareTo(maximum.get());
        return (includeMinimum ? comparedToMinimum >= 0 : comparedToMinimum > 0)
            && (includeMaximum ? comparedToMaximum <= 0 : comparedToMaximum < 0);
    }

}
