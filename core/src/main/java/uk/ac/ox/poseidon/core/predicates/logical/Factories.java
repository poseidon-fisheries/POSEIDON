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

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.MappingAsValue;
import uk.ac.ox.poseidon.core.functions.MappingAsValueFactory;
import uk.ac.ox.poseidon.core.functions.ValueAsMappingFactory;

/**
 * Factories for yes/no values and mappings combining other yes/no values or
 * mappings. Each combinator has one overload per shape: combining values gives
 * a value, combining mappings gives a mapping, and the two cannot be mixed. The
 * value is the mapping, with nothing to map, so both mean the same. For
 * a constant yes/no mapping, use
 * {@link uk.ac.ox.poseidon.core.providers.constant.Factories#always(Object)
 * always(true)} or {@code always(false)}.
 */
@SuppressWarnings({ "Convert2Diamond", "overloads", "varargs" })
public final class Factories {

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
   * @return a {@link ValueFactory} for an {@link AllOf} over the resolved
   *         predicates, made a value by {@link MappingAsValue}
   * @see AllOf
   */
  @SafeVarargs
  public static <S extends Scope, C> MappingAsValueFactory<S, C, Boolean> allOf(
    final ValueFactory<? super S, ? super C, Boolean>... predicates
  ) {
    return new MappingAsValueFactory<S, C, Boolean>(
      new AllOfFactory<S, C, Void>(
        Arrays.stream(predicates)
          .<MappingFactory<? super S, ? super C, ? super Void, Boolean>>map(
            predicate -> new ValueAsMappingFactory<S, C, Boolean>(predicate)
          )
          .collect(toImmutableList())
      )
    );
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
   * @return a {@link ValueFactory} for an {@link AnyOf} over the resolved
   *         predicates, made a value by {@link MappingAsValue}
   * @see AnyOf
   */
  @SafeVarargs
  public static <S extends Scope, C> MappingAsValueFactory<S, C, Boolean> anyOf(
    final ValueFactory<? super S, ? super C, Boolean>... predicates
  ) {
    return new MappingAsValueFactory<S, C, Boolean>(
      new AnyOfFactory<S, C, Void>(
        Arrays.stream(predicates)
          .<MappingFactory<? super S, ? super C, ? super Void, Boolean>>map(
            predicate -> new ValueAsMappingFactory<S, C, Boolean>(predicate)
          )
          .collect(toImmutableList())
      )
    );
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
   * @return a {@link ValueFactory} for a {@link Not} wrapping the resolved
   *         value, made a value by {@link MappingAsValue}
   * @see Not
   */
  public static <S extends Scope, C> MappingAsValueFactory<S, C, Boolean> not(
    final ValueFactory<? super S, ? super C, Boolean> predicate
  ) {
    return new MappingAsValueFactory<S, C, Boolean>(
      new NotFactory<S, C, Void>(
        new ValueAsMappingFactory<S, C, Boolean>(predicate)
      )
    );
  }
}
