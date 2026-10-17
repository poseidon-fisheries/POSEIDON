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

package uk.ac.ox.poseidon.core.providers.constant;

import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;

/**
 * A mapping whose result is the same whatever the context and whatever it maps,
 * such as a filter that lets every option through. Built via
 * {@link Factories#alwaysTrue()} and {@link Factories#alwaysFalse()}.
 *
 * @param <R> the type of the result
 */
@RequiredArgsConstructor
public class ConstantMapping<R> implements BiFunction<Object, Object, R> {

  private final R result;

  @Override
  public R apply(final Object context, final Object mapped) {
    return result;
  }
}
