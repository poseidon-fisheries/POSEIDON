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
 * A value read from a mapping that has nothing to map: on each call, it asks
 * the mapping under the same context, with {@code null}, the only value of
 * {@link Void}, as the thing mapped. A combinator that makes sense in both
 * shapes, such as a conjunction, is written once, as a mapping; given values,
 * it combines them made mappings by {@link ValueAsMapping}, and is made a value
 * by this. It then means the same in both shapes. Built by the value overloads
 * of those combinators, and of the blocks that take their input, never by
 * scenario code.
 *
 * @param <C> the type of the context
 * @param <R> the type of the value
 */
@RequiredArgsConstructor
public class MappingAsValue<C, R> implements Function<C, R> {

  private final @NonNull BiFunction<
    ? super C,
    ? super Void,
    ? extends R
  > mapping;

  @Override
  public R apply(final C context) {
    return mapping.apply(context, null);
  }
}
