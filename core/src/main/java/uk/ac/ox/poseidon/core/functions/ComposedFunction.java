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

package uk.ac.ox.poseidon.core.functions;

import java.util.function.Function;

/**
 * A {@link Function} equivalent to applying two other functions in sequence
 * ({@code function2.compose(function1)}). Built via
 * {@link Factories#composedFunction(uk.ac.ox.poseidon.core.Factory,
 * uk.ac.ox.poseidon.core.Factory)} in this package.
 *
 * @param <T1> the type of the composed function's input
 * @param <T2> the intermediate type produced by {@code function1} and consumed by
 *             {@code function2}
 * @param <R>  the type of the composed function's output
 */
public class ComposedFunction<T1, T2, R> implements Function<T1, R> {

    private final Function<? super T1, ? extends R> composedFunction;

    /**
     * @param function1 the function applied first, {@code T1 -> T2}
     * @param function2 the function applied to {@code function1}'s result, {@code T2 -> R}
     */
    public ComposedFunction(
        final Function<? super T1, ? extends T2> function1,
        final Function<? super T2, ? extends R> function2
    ) {
        this.composedFunction = function2.compose(function1);
    }

    @Override
    public R apply(final T1 t1) {
        return composedFunction.apply(t1);
    }

}
