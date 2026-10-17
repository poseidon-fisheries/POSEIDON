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

package uk.ac.ox.poseidon.core.predicates;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.util.Collection;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for conditions, which are values or mappings, and for yes/no
 * mappings testing the thing mapped against values read under the context.
 */
@SuppressWarnings("Convert2Diamond")
public class Factories {

  private Factories() {}

  /**
   * @param extractor the mapping applied to the thing tested before checking
   *                  {@code predicate}
   * @param predicate the yes/no mapping the extracted value is tested against
   * @return a {@link MappingFactory} for a {@link Condition} that composes the
   *         given extractor and predicate
   * @see Condition
   */
  public static <S extends Scope, C, T, U> ConditionFactory<
    S,
    C,
    T,
    U
  > condition(
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends U
    > extractor,
    final MappingFactory<? super S, ? super C, ? super U, Boolean> predicate
  ) {
    return new ConditionFactory<S, C, T, U>(extractor, predicate);
  }

  /**
   * @param extractor the value read under the context and then tested
   * @param predicate the yes/no mapping the value is tested against
   * @return a {@link ValueFactory} for a {@link ConditionValue} that composes
   *         the given value and predicate
   * @see ConditionValue
   */
  public static <S extends Scope, C, U> ConditionValueFactory<
    S,
    C,
    U
  > condition(
    final ValueFactory<? super S, ? super C, ? extends U> extractor,
    final MappingFactory<? super S, ? super C, ? super U, Boolean> predicate
  ) {
    return new ConditionValueFactory<S, C, U>(extractor, predicate);
  }

  /**
   * @param value the value the resulting predicate tests against
   * @return a {@link MappingFactory} for an {@link Equal} predicate over the
   *         given literal value
   * @see Equal
   */
  public static <S extends Scope, T> EqualFactory<S, Object, T> equal(
    final T value
  ) {
    return equal(constant(value));
  }

  /**
   * @param value factory for the value the resulting predicate tests against
   * @return a {@link MappingFactory} for an {@link Equal} predicate over the
   *         resolved value
   * @see Equal
   */
  public static <S extends Scope, C, T> EqualFactory<S, C, T> equal(
    final ValueFactory<? super S, ? super C, ? extends T> value
  ) {
    return new EqualFactory<>(value);
  }

  /**
   * @param values factory for the collection the resulting predicate tests
   *               membership against
   * @return a {@link MappingFactory} for an {@link InSet} predicate over the
   *         resolved collection
   * @see InSet
   */
  public static <S extends Scope, C, T> InSetFactory<S, C, T> in(
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends Collection<? extends T>
    > values
  ) {
    return new InSetFactory<>(values);
  }
}
