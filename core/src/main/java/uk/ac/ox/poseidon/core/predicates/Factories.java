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

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

/**
 * Factories for {@link Predicate}s that test a value derived from, or drawn against, other
 * factory-resolved inputs.
 */
public class Factories {

    private Factories() {}

    /**
     * @param extractor the function applied to the tested input before checking {@code predicate}
     * @param predicate the predicate the extracted value is tested against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link Condition} that
     * composes the given extractor and predicate
     * @see Condition
     */
    public static <S extends Scope, T, U> ConditionFactory<S, T, U> condition(
        final Factory<? super S, ? extends Function<? super T, U>> extractor,
        final Factory<? super S, ? extends Predicate<? super U>> predicate
    ) {
        return new ConditionFactory<>(extractor, predicate);
    }

    /**
     * @param value the value the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link Equal} predicate
     * over the given literal value
     * @see Equal
     */
    public static <S extends Scope, T> EqualFactory<S, T> equal(final T value) {
        return equal(constant(value));
    }

    /**
     * @param value factory for the value the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link Equal} predicate
     * over the resolved value
     * @see Equal
     */
    public static <S extends Scope, T> EqualFactory<S, T> equal(
        final Factory<? super S, ? extends Supplier<? extends T>> value
    ) {
        return new EqualFactory<>(value);
    }

    /**
     * @param values factory for the collection the resulting predicate tests membership against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link InSet} predicate
     * over the resolved collection
     * @see InSet
     */
    public static <S extends Scope, T> InSetFactory<S, T> in(
        final Factory<? super S, ? extends Supplier<? extends Collection<? extends T>>> values
    ) {
        return new InSetFactory<>(values);
    }

}
