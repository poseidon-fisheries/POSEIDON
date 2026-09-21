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

package uk.ac.ox.poseidon.core.predicates.logical;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.List;
import java.util.function.Predicate;

/**
 * Factories for {@link Predicate}s that combine other predicates. For an always-true/always-false
 * leaf predicate to combine with these, see
 * {@link uk.ac.ox.poseidon.core.providers.constant.Factories#alwaysTrue()}/
 * {@link uk.ac.ox.poseidon.core.providers.constant.Factories#alwaysFalse()} — a
 * {@code BooleanProvider} already doubles as a {@code Predicate<Object>}.
 */
@SuppressWarnings("Convert2Diamond")
public class Factories {
    private Factories() {
    }

    /**
     * @param predicates the predicates to combine
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link AllOf} of the
     * given predicates
     * @see AllOf
     */
    @SafeVarargs
    public static <S extends Scope, T> AllOfFactory<S, T> allOf(
        final Factory<? super S, ? extends Predicate<? super T>>... predicates
    ) {
        return new AllOfFactory<S, T>(List.of(predicates));
    }

    /**
     * @param predicates the predicates to combine
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link AnyOf} of the
     * given predicates
     * @see AnyOf
     */
    @SafeVarargs
    public static <S extends Scope, T> AnyOfFactory<S, T> anyOf(
        final Factory<? super S, ? extends Predicate<? super T>>... predicates
    ) {
        return new AnyOfFactory<S, T>(List.of(predicates));
    }

    /**
     * @param predicate the predicate to negate
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link Not} of the
     * given predicate
     * @see Not
     */
    public static <S extends Scope, T> NotFactory<S, T> not(
        final Factory<? super S, ? extends Predicate<? super T>> predicate
    ) {
        return new NotFactory<S, T>(predicate);
    }
}
