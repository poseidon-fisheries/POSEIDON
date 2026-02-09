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

@SuppressWarnings("Convert2Diamond")
public class Factories {
    private Factories() {
    }

    @SafeVarargs
    public static <S extends Scope, T> AllOfFactory<S, T> allOf(
        final Factory<? super S, ? extends Predicate<? super T>>... predicates
    ) {
        return new AllOfFactory<S, T>(List.of(predicates));
    }

    @SafeVarargs
    public static <S extends Scope, T> AnyOfFactory<S, T> anyOf(
        final Factory<? super S, ? extends Predicate<? super T>>... predicates
    ) {
        return new AnyOfFactory<S, T>(List.of(predicates));
    }

    public static <S extends Scope, T> NotFactory<S, T> not(
        final Factory<? super S, ? extends Predicate<? super T>> predicate
    ) {
        return new NotFactory<S, T>(predicate);
    }

    public static AlwaysTrueFactory alwaysTrue() {
        return new AlwaysTrueFactory();
    }

    public static AlwaysFalseFactory alwaysFalse() {
        return new AlwaysFalseFactory();
    }
}
