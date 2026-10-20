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

import static uk.ac.ox.poseidon.core.utils.Factories.object;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * Factories for values and mappings that are the same whatever they are asked.
 */
public final class Factories {

  private Factories() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * @param value the value
   * @return a {@link uk.ac.ox.poseidon.core.ValueFactory} for a
   *         {@link Constant} of the given value; where a mapping is expected
   *         instead (a filter, a part of a key), use {@link #always}
   * @see Constant
   */
  public static <S extends Scope, R> ConstantFactory<S, R> constant(
    final R value
  ) {
    return new ConstantFactory<>(object(value));
  }

  /**
   * @param value factory for the value
   * @return a {@link uk.ac.ox.poseidon.core.ValueFactory} for a
   *         {@link Constant} of the resolved value; where a mapping is
   *         expected instead, use {@link #always}
   * @see Constant
   */
  public static <S extends Scope, R> ConstantFactory<S, R> constant(
    final Factory<? super S, ? extends R> value
  ) {
    return new ConstantFactory<>(value);
  }

  /**
   * @param result the result
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for a
   *         {@link ConstantMapping} that always gives the given result,
   *         whatever it maps: {@code always(true)} as a filter letting
   *         everything through, {@code always(GEAR_CODE)} as one part of a
   *         key; where a value is expected instead, use {@link #constant}
   * @see ConstantMapping
   */
  public static <S extends Scope, R> ConstantMappingFactory<S, R> always(
    final R result
  ) {
    return new ConstantMappingFactory<>(object(result));
  }

  /**
   * @param result factory for the result
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for a
   *         {@link ConstantMapping} that always gives the resolved result,
   *         whatever it maps, e.g. {@code always(hours(1))} as one term of a
   *         sum over routes; where a value is expected instead, use
   *         {@link #constant}
   * @see ConstantMapping
   */
  public static <S extends Scope, R> ConstantMappingFactory<S, R> always(
    final Factory<? super S, ? extends R> result
  ) {
    return new ConstantMappingFactory<>(result);
  }
}
