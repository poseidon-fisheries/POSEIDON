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
 * A value made a mapping that has nothing to map: on each call, it reads the
 * value under the same context and ignores the thing mapped, which is always
 * {@code null}, the only value of {@link Void}. Typed by {@code Void}, it fits
 * only where nothing is mapped: inside a combinator that {@link MappingAsValue}
 * makes a value again. Built by the value overloads of the combinators, and of
 * the blocks that take their input, never by scenario code.
 *
 * @param <C> the type of the context
 * @param <R> the type of the value
 */
@RequiredArgsConstructor
public class ValueAsMapping<C, R> implements BiFunction<C, Void, R> {

  private final @NonNull Function<? super C, ? extends R> value;

  @Override
  public R apply(final C context, final Void nothing) {
    return value.apply(context);
  }
}
