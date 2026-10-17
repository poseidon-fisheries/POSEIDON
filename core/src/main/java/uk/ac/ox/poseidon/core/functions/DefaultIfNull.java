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
import lombok.RequiredArgsConstructor;

/**
 * A mapping that delegates to another mapping, under the same context, and
 * substitutes a fixed default whenever that delegate returns {@code null}.
 * Built via {@link Factories Factories.defaultIfNull(...)} in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 * @param <R> the type of the result, and of the default value
 */
@RequiredArgsConstructor
public class DefaultIfNull<C, T, R> implements BiFunction<C, T, R> {

  private final BiFunction<? super C, ? super T, ? extends R> delegate;
  private final R defaultValue;

  @Override
  public R apply(final C context, final T t) {
    final R result = delegate.apply(context, t);
    return result == null ? defaultValue : result;
  }
}
