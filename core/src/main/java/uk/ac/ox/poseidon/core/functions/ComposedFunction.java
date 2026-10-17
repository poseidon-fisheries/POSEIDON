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

import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A mapping equivalent to applying two other mappings in sequence, both under
 * the same context. Built via
 * {@link Factories#composedFunction(uk.ac.ox.poseidon.core.MappingFactory, uk.ac.ox.poseidon.core.MappingFactory)}
 * in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 * @param <U> the intermediate type produced by {@code function1} and mapped by {@code function2}
 * @param <R> the type of the result
 */
@RequiredArgsConstructor
public class ComposedFunction<C, T, U, R> implements BiFunction<C, T, R> {

  private final @NonNull BiFunction<
    ? super C,
    ? super T,
    ? extends U
  > function1;
  private final @NonNull BiFunction<
    ? super C,
    ? super U,
    ? extends R
  > function2;

  @Override
  public R apply(final C context, final T t) {
    return function2.apply(context, function1.apply(context, t));
  }
}
