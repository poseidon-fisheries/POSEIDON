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

/**
 * Factories for {@link java.util.function.Predicate}s that compare a tested value against one or
 * two thresholds.
 */
public class Factories {
    private Factories() {}

    /**
     * @param threshold the value the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link LessThan}
     * predicate over the given literal threshold
     * @see LessThan
     */
    public static <S extends Scope> LessThanFactory<S, Double> lessThan(final double threshold) {
        return lessThan(constant(threshold));
    }

    /**
     * @param value factory for the value the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link LessThan}
     * predicate over the resolved threshold
     * @see LessThan
     */
    public static <S extends Scope, T> LessThanFactory<S, T> lessThan(
        final Factory<? super S, ? extends Supplier<? extends T>> value
    ) {
        return new LessThanFactory<>(value);
    }

    /**
     * @param threshold the value the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link GreaterThan}
     * predicate over the given literal threshold
     * @see GreaterThan
     */
    public static <S extends Scope> GreaterThanFactory<S, Double> greaterThan(final double threshold) {
        return greaterThan(constant(threshold));
    }

    /**
     * @param value factory for the value the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link GreaterThan}
     * predicate over the resolved threshold
     * @see GreaterThan
     */
    public static <S extends Scope, T> GreaterThanFactory<S, T> greaterThan(
        final Factory<? super S, ? extends Supplier<? extends T>> value
    ) {
        return new GreaterThanFactory<>(value);
    }

    /**
     * @param minimum inclusive lower bound
     * @param maximum inclusive upper bound
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link Between}
     * predicate over the given literal bounds
     * @see Between
     */
    public static <S extends Scope> BetweenFactory<S, Double> between(
        final double minimum,
        final double maximum
    ) {
        return between(minimum, maximum, true, true);
    }

    /**
     * @param minimum        lower bound
     * @param maximum        upper bound
     * @param includeMinimum whether {@code minimum} itself satisfies the resulting predicate
     * @param includeMaximum whether {@code maximum} itself satisfies the resulting predicate
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link Between}
     * predicate over the given literal bounds
     * @see Between
     */
    public static <S extends Scope> BetweenFactory<S, Double> between(
        final double minimum,
        final double maximum,
        final boolean includeMinimum,
        final boolean includeMaximum
    ) {
        return between(constant(minimum), constant(maximum), includeMinimum, includeMaximum);
    }

    /**
     * @param minimum factory for the inclusive lower bound
     * @param maximum factory for the inclusive upper bound
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link Between}
     * predicate over the resolved bounds
     * @see Between
     */
    public static <S extends Scope, T> BetweenFactory<S, T> between(
        final Factory<? super S, ? extends Supplier<? extends T>> minimum,
        final Factory<? super S, ? extends Supplier<? extends T>> maximum
    ) {
        return between(minimum, maximum, true, true);
    }

    /**
     * @param minimum        factory for the lower bound
     * @param maximum        factory for the upper bound
     * @param includeMinimum whether {@code minimum} itself satisfies the resulting predicate
     * @param includeMaximum whether {@code maximum} itself satisfies the resulting predicate
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link Between}
     * predicate over the resolved bounds
     * @see Between
     */
    public static <S extends Scope, T> BetweenFactory<S, T> between(
        final Factory<? super S, ? extends Supplier<? extends T>> minimum,
        final Factory<? super S, ? extends Supplier<? extends T>> maximum,
        final boolean includeMinimum,
        final boolean includeMaximum
    ) {
        return new BetweenFactory<>(minimum, maximum, includeMinimum, includeMaximum);
    }

}
