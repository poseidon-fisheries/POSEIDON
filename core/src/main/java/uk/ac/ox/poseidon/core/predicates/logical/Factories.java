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

package uk.ac.ox.poseidon.core.predicates.logical;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for yes/no values and mappings combining other yes/no values or
 * mappings. Each combinator has one overload per shape: combining values gives
 * a value, combining mappings gives a mapping, and the two cannot be mixed. For
 * a constant yes/no mapping, use
 * {@link uk.ac.ox.poseidon.core.providers.constant.Factories#alwaysTrue()} or
 * {@link uk.ac.ox.poseidon.core.providers.constant.Factories#alwaysFalse()}.
 */
@SuppressWarnings("Convert2Diamond")
public class Factories {

  private Factories() {}

  /**
   * @param predicates factories for the component mappings
   * @return a {@link MappingFactory} for an {@link AllOf} over the resolved
   *         predicates
   * @see AllOf
   */
  @SafeVarargs
  public static <S extends Scope, C, T> AllOfFactory<S, C, T> allOf(
    final MappingFactory<? super S, ? super C, ? super T, Boolean>... predicates
  ) {
    return new AllOfFactory<S, C, T>(ImmutableList.copyOf(predicates));
  }

  /**
   * @param predicates factories for the component values
   * @return a {@link ValueFactory} for an {@link AllOfValue} over the
   *         resolved predicates
   * @see AllOfValue
   */
  @SafeVarargs
  public static <S extends Scope, C> AllOfValueFactory<S, C> allOf(
    final ValueFactory<? super S, ? super C, Boolean>... predicates
  ) {
    return new AllOfValueFactory<S, C>(ImmutableList.copyOf(predicates));
  }

  /**
   * @param predicates factories for the component mappings
   * @return a {@link MappingFactory} for an {@link AnyOf} over the resolved
   *         predicates
   * @see AnyOf
   */
  @SafeVarargs
  public static <S extends Scope, C, T> AnyOfFactory<S, C, T> anyOf(
    final MappingFactory<? super S, ? super C, ? super T, Boolean>... predicates
  ) {
    return new AnyOfFactory<S, C, T>(ImmutableList.copyOf(predicates));
  }

  /**
   * @param predicates factories for the component values
   * @return a {@link ValueFactory} for an {@link AnyOfValue} over the
   *         resolved predicates
   * @see AnyOfValue
   */
  @SafeVarargs
  public static <S extends Scope, C> AnyOfValueFactory<S, C> anyOf(
    final ValueFactory<? super S, ? super C, Boolean>... predicates
  ) {
    return new AnyOfValueFactory<S, C>(ImmutableList.copyOf(predicates));
  }

  /**
   * @param predicate factory for the mapping to negate
   * @return a {@link MappingFactory} for a {@link Not} wrapping the resolved
   *         mapping
   * @see Not
   */
  public static <S extends Scope, C, T> NotFactory<S, C, T> not(
    final MappingFactory<? super S, ? super C, ? super T, Boolean> predicate
  ) {
    return new NotFactory<S, C, T>(predicate);
  }

  /**
   * @param predicate factory for the value to negate
   * @return a {@link ValueFactory} for a {@link NotValue} wrapping the
   *         resolved value
   * @see NotValue
   */
  public static <S extends Scope, C> NotValueFactory<S, C> not(
    final ValueFactory<? super S, ? super C, Boolean> predicate
  ) {
    return new NotValueFactory<S, C>(predicate);
  }
}
