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
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A value that reads another value under the context, then maps it under the
 * same context. Built via
 * {@link Factories#composedFunction(uk.ac.ox.poseidon.core.ValueFactory, uk.ac.ox.poseidon.core.MappingFactory)}
 * in this package.
 *
 * @param <C> the type of the context
 * @param <U> the type of the value read first and mapped by {@code function2}
 * @param <R> the type of the result
 */
@RequiredArgsConstructor
public class ComposedFunctionValue<C, U, R> implements Function<C, R> {

  private final @NonNull Function<? super C, ? extends U> function1;
  private final @NonNull BiFunction<
    ? super C,
    ? super U,
    ? extends R
  > function2;

  @Override
  public R apply(final C context) {
    return function2.apply(context, function1.apply(context));
  }
}
