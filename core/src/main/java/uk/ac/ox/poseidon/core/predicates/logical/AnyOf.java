/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.predicates.logical;

import lombok.NonNull;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AnyOf<T> implements Predicate<T> {

    // Using an array for performance reasons
    @NonNull private final Predicate<? super T>[] predicates;

    @SuppressWarnings("unchecked")
    public AnyOf(final Collection<? extends Predicate<? super T>> predicates) {
        this.predicates = predicates.toArray(Predicate[]::new);
    }

    public Stream<Predicate<? super T>> getPredicates() {
        return Stream.of(predicates);
    }

    @Override
    public boolean test(final T t) {
        for (final Predicate<? super T> predicate : predicates) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }
}
