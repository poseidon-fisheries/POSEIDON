/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core.predicates.logical;

import lombok.NonNull;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AllOf<T> implements Predicate<T> {

    // Using an array for performance reason (and not exposing outside the class)
    @NonNull private final Predicate<T>[] predicates;

    @SuppressWarnings("unchecked")
    public AllOf(@NonNull final Collection<Predicate<T>> predicates) {
        this.predicates = predicates.toArray(Predicate[]::new);
    }

    public Stream<Predicate<T>> getPredicates() {
        return Stream.of(predicates);
    }

    @Override
    public boolean test(final T t) {
        for (final Predicate<T> predicate : predicates) {
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }
}
