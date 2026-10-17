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
public class Factories {

  private Factories() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for a
   *         {@link ConstantMapping} that always answers {@code true}, such as a
   *         filter letting everything through
   * @see ConstantMapping
   */
  public static <S extends Scope> ConstantMappingFactory<
    S,
    Boolean
  > alwaysTrue() {
    return new ConstantMappingFactory<>(object(true));
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for a
   *         {@link ConstantMapping} that always answers {@code false}
   * @see ConstantMapping
   */
  public static <S extends Scope> ConstantMappingFactory<
    S,
    Boolean
  > alwaysFalse() {
    return new ConstantMappingFactory<>(object(false));
  }

  /**
   * @param value the value
   * @return a {@link uk.ac.ox.poseidon.core.ValueFactory} for a
   *         {@link Constant} of the given value
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
   *         {@link Constant} of the resolved value
   * @see Constant
   */
  public static <S extends Scope, R> ConstantFactory<S, R> constant(
    final Factory<? super S, ? extends R> value
  ) {
    return new ConstantFactory<>(value);
  }
}
