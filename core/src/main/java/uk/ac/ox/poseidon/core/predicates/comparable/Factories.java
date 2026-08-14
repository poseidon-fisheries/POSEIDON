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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

public class Factories {
    private Factories() {}

    public static <S extends Scope> LessThanFactory<S, Double> lessThan(final double threshold) {
        return lessThan(constant(threshold));
    }

    public static <S extends Scope, T> LessThanFactory<S, T> lessThan(
        final Factory<? super S, ? extends Supplier<? extends T>> value
    ) {
        return new LessThanFactory<>(value);
    }

    public static <S extends Scope> GreaterThanFactory<S, Double> greaterThan(final double threshold) {
        return greaterThan(constant(threshold));
    }

    public static <S extends Scope, T> GreaterThanFactory<S, T> greaterThan(
        final Factory<? super S, ? extends Supplier<? extends T>> value
    ) {
        return new GreaterThanFactory<>(value);
    }

    public static <S extends Scope> BetweenFactory<S, Double> between(
        final double minimum,
        final double maximum
    ) {
        return between(minimum, maximum, true, true);
    }

    public static <S extends Scope> BetweenFactory<S, Double> between(
        final double minimum,
        final double maximum,
        final boolean includeMinimum,
        final boolean includeMaximum
    ) {
        return between(constant(minimum), constant(maximum), includeMinimum, includeMaximum);
    }

    public static <S extends Scope, T> BetweenFactory<S, T> between(
        final Factory<? super S, ? extends Supplier<? extends T>> minimum,
        final Factory<? super S, ? extends Supplier<? extends T>> maximum
    ) {
        return between(minimum, maximum, true, true);
    }

    public static <S extends Scope, T> BetweenFactory<S, T> between(
        final Factory<? super S, ? extends Supplier<? extends T>> minimum,
        final Factory<? super S, ? extends Supplier<? extends T>> maximum,
        final boolean includeMinimum,
        final boolean includeMaximum
    ) {
        return new BetweenFactory<>(minimum, maximum, includeMinimum, includeMaximum);
    }

}
