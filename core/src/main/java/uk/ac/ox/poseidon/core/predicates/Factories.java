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

package uk.ac.ox.poseidon.core.predicates;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Factories {

    private Factories() {}

    public static <S extends Scope, T, U> ConditionFactory<S, T, U> condition(
        final Factory<? super S, ? extends Function<? super T, U>> extractor,
        final Factory<? super S, ? extends Predicate<? super U>> predicate
    ) {
        return new ConditionFactory<>(extractor, predicate);
    }

    public static <S extends Scope, T> InSetFactory<S, T> in(
        final Factory<? super S, ? extends Supplier<? extends Collection<? extends T>>> values
    ) {
        return new InSetFactory<>(values);
    }

}
