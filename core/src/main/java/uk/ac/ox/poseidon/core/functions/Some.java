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

import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * A value that is always present: the value of a delegate, under the same
 * context, wrapped in an {@link Optional}. It lets a value that always exists
 * stand where a value that may be missing is expected. Built via
 * {@link Factories Factories.some(...)} in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the value
 */
@RequiredArgsConstructor
public class Some<C, T> implements Function<C, Optional<T>> {

  private final Function<? super C, ? extends T> delegate;

  /**
   * @throws NullPointerException if the delegate gives {@code null}
   */
  @Override
  public Optional<T> apply(final C context) {
    return Optional.of(delegate.apply(context));
  }
}
