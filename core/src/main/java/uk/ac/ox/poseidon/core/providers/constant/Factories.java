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
import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * Factories for {@link uk.ac.ox.poseidon.core.providers.Provider}s that always
 * return a fixed value.
 */
public class Factories {

  private Factories() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link ConstantBoolean} that always returns {@code true}
   * @see ConstantBoolean
   */
  public static ConstantBooleanFactory alwaysTrue() {
    return new ConstantBooleanFactory(true);
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link ConstantBoolean} that always returns {@code false}
   * @see ConstantBoolean
   */
  public static ConstantBooleanFactory alwaysFalse() {
    return new ConstantBooleanFactory(false);
  }

  /**
   * @param value the value the resulting provider will always return
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link ConstantDouble} wrapping the given literal value
   * @see ConstantDouble
   */
  public static <S extends Scope> ConstantDoubleFromValueFactory<S> constantDouble(
    final double value
  ) {
    return new ConstantDoubleFromValueFactory<>(value);
  }

  /**
   * @param doubleFactory factory for the value the resulting provider will
   *                      always return
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link ConstantDouble} wrapping the resolved value
   * @see ConstantDouble
   */
  public static <S extends Scope> ConstantDoubleFactory<S> constantDouble(
    final Factory<? super S, ? extends Double> doubleFactory
  ) {
    return new ConstantDoubleFactory<>(doubleFactory);
  }

  /**
   * @param value the value the resulting provider will always return
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link ConstantInt} wrapping the given literal value
   * @see ConstantInt
   */
  public static <S extends Scope> ConstantIntFromValueFactory<S> constantInt(
    final int value
  ) {
    return new ConstantIntFromValueFactory<>(value);
  }

  /**
   * @param integerFactory factory for the value the resulting provider will
   *                       always return
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link ConstantInt} wrapping the resolved value
   * @see ConstantInt
   */
  public static <S extends Scope> ConstantIntFactory<S> constantInt(
    final Factory<? super S, ? extends Integer> integerFactory
  ) {
    return new ConstantIntFactory<>(integerFactory);
  }

  /**
   * @param value the value the resulting provider will always return
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link Constant} wrapping the given literal value
   * @see Constant
   */
  public static <S extends Scope, T> ConstantFactory<S, T> constant(
    final T value
  ) {
    return new ConstantFactory<>(object(value));
  }

  /**
   * @param value factory for the value the resulting provider will always
   *              return
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link Constant} wrapping the resolved value
   * @see Constant
   */
  public static <S extends Scope, T> ConstantFactory<S, T> constant(
    final Factory<? super S, ? extends T> value
  ) {
    return new ConstantFactory<>(value);
  }
}
