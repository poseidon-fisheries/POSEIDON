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

import static uk.ac.ox.poseidon.core.utils.Factories.object;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.Map;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for values and mappings that transform, combine, or look up other
 * values and mappings. A combinator has one overload per shape: built from a
 * value, it gives a value; built from mappings, a mapping.
 */
@SuppressWarnings("Convert2Diamond")
public class Factories {

  private Factories() {}

  /**
   * @param map factory for the map to look values up in
   * @return a {@link MappingFactory} for a
   *         {@link MapLookup} over the resolved map
   * @see MapLookup
   */
  public static <S extends Scope, K, V> MapLookupFactory<S, K, V> mapLookup(
    final Factory<? super S, ? extends Map<? super K, ? extends V>> map
  ) {
    return new MapLookupFactory<>(map);
  }

  /**
   * @param map factory for the map to look the value up in
   * @param key the fixed key to look up
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for the value
   *         found under {@code key} in the resolved map
   * @see MapEntryFactory
   */
  public static <S extends Scope, K, V> MapEntryFactory<S, K, V> mapEntry(
    final Factory<? super S, ? extends Map<? super K, ? extends V>> map,
    final K key
  ) {
    return new MapEntryFactory<>(map, key);
  }

  /**
   * @param function1 the mapping applied first
   * @param function2 the mapping applied to {@code function1}'s result
   * @return a {@link MappingFactory} for a {@link ComposedFunction} of the
   *         resolved mappings
   * @see ComposedFunction
   */
  public static <S extends Scope, C, T, U, R> ComposedFunctionFactory<
    S,
    C,
    T,
    U,
    R
  > composedFunction(
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends U
    > function1,
    final MappingFactory<? super S, ? super C, ? super U, ? extends R> function2
  ) {
    return new ComposedFunctionFactory<S, C, T, U, R>(function1, function2);
  }

  /**
   * @param function1 the value read first
   * @param function2 the mapping applied to that value
   * @return a {@link ValueFactory} for a {@link ComposedFunctionValue} of the
   *         resolved value and mapping
   * @see ComposedFunctionValue
   */
  public static <S extends Scope, C, U, R> ComposedFunctionValueFactory<
    S,
    C,
    U,
    R
  > composedFunction(
    final ValueFactory<? super S, ? super C, ? extends U> function1,
    final MappingFactory<? super S, ? super C, ? super U, ? extends R> function2
  ) {
    return new ComposedFunctionValueFactory<S, C, U, R>(function1, function2);
  }

  /**
   * @param durations the duration-producing mappings to sum
   * @return a {@link MappingFactory} for a {@link CombinedDuration} of the
   *         resolved mappings
   * @see CombinedDuration
   */
  @SafeVarargs
  public static <S extends Scope, C, T> CombinedDurationFactory<
    S,
    C,
    T
  > combinedDuration(
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends Duration
    >... durations
  ) {
    return new CombinedDurationFactory<S, C, T>(
      ImmutableList.copyOf(durations)
    );
  }

  /**
   * @param durations the duration values to sum
   * @return a {@link ValueFactory} for a {@link CombinedDurationValue} of the
   *         resolved values
   * @see CombinedDurationValue
   */
  @SafeVarargs
  public static <S extends Scope, C> CombinedDurationValueFactory<
    S,
    C
  > combinedDuration(
    final ValueFactory<? super S, ? super C, ? extends Duration>... durations
  ) {
    return new CombinedDurationValueFactory<S, C>(
      ImmutableList.copyOf(durations)
    );
  }

  /**
   * @param delegate     the mapping to delegate to
   * @param defaultValue factory for the value substituted when {@code delegate}
   *                     returns {@code null}
   * @return a {@link MappingFactory} for a {@link DefaultIfNull} wrapping the
   *         resolved delegate and default
   * @see DefaultIfNull
   */
  public static <S extends Scope, C, T, R> DefaultIfNullFactory<
    S,
    C,
    T,
    R
  > defaultIfNull(
    final MappingFactory<? super S, ? super C, ? super T, ? extends R> delegate,
    final Factory<? super S, ? extends R> defaultValue
  ) {
    return new DefaultIfNullFactory<S, C, T, R>(delegate, defaultValue);
  }

  /**
   * @param delegate     the mapping to delegate to
   * @param defaultValue the literal value substituted when {@code delegate}
   *                     returns {@code null}
   * @return a {@link MappingFactory} for a {@link DefaultIfNull} wrapping the
   *         resolved delegate and the given default
   * @see DefaultIfNull
   */
  public static <S extends Scope, C, T, R> DefaultIfNullFactory<
    S,
    C,
    T,
    R
  > defaultIfNull(
    final MappingFactory<? super S, ? super C, ? super T, ? extends R> delegate,
    final R defaultValue
  ) {
    return defaultIfNull(delegate, object(defaultValue));
  }

  /**
   * @param delegate     the value to read
   * @param defaultValue factory for the value substituted when {@code delegate}
   *                     is {@code null}
   * @return a {@link ValueFactory} for a {@link DefaultIfNullValue} wrapping
   *         the resolved delegate and default
   * @see DefaultIfNullValue
   */
  public static <S extends Scope, C, R> DefaultIfNullValueFactory<
    S,
    C,
    R
  > defaultIfNull(
    final ValueFactory<? super S, ? super C, ? extends R> delegate,
    final Factory<? super S, ? extends R> defaultValue
  ) {
    return new DefaultIfNullValueFactory<S, C, R>(delegate, defaultValue);
  }

  /**
   * @param delegate     the value to read
   * @param defaultValue the literal value substituted when {@code delegate} is
   *                     {@code null}
   * @return a {@link ValueFactory} for a {@link DefaultIfNullValue} wrapping
   *         the resolved delegate and the given default
   * @see DefaultIfNullValue
   */
  public static <S extends Scope, C, R> DefaultIfNullValueFactory<
    S,
    C,
    R
  > defaultIfNull(
    final ValueFactory<? super S, ? super C, ? extends R> delegate,
    final R defaultValue
  ) {
    return defaultIfNull(delegate, object(defaultValue));
  }

  /**
   * @param functions the mappings to apply to the same thing and join into one
   *                  key
   * @return a {@link MappingFactory} for a {@link MultiStringKeyFromFunctions}
   *         of the resolved mappings
   * @see MultiStringKeyFromFunctions
   */
  @SafeVarargs
  public static <S extends Scope, C, T> MultiStringKeyFromFunctionsFactory<
    S,
    C,
    T
  > multiStringKeyFromFunctions(
    final MappingFactory<? super S, ? super C, ? super T, ?>... functions
  ) {
    return new MultiStringKeyFromFunctionsFactory<S, C, T>(
      ImmutableList.copyOf(functions)
    );
  }

  /**
   * @param functions the values to read and join into one key
   * @return a {@link ValueFactory} for a
   *         {@link MultiStringKeyFromFunctionsValue} of the resolved values
   * @see MultiStringKeyFromFunctionsValue
   */
  @SafeVarargs
  public static <S extends Scope, C> MultiStringKeyFromFunctionsValueFactory<
    S,
    C
  > multiStringKeyFromFunctions(
    final ValueFactory<? super S, ? super C, ?>... functions
  ) {
    return new MultiStringKeyFromFunctionsValueFactory<S, C>(
      ImmutableList.copyOf(functions)
    );
  }

  /**
   * @param intervals the non-overlapping intervals to map numbers into (see
   *                  {@link NumericIntervalToStringMapperFactory#interval})
   * @return a {@link MappingFactory} for a {@link NumericIntervalMapper} over
   *         the given intervals
   * @see NumericIntervalMapper
   */
  public static NumericIntervalToStringMapperFactory numericIntervalToStringMapper(
    final NumericIntervalToStringMapperFactory.Interval... intervals
  ) {
    return new NumericIntervalToStringMapperFactory(
      ImmutableList.copyOf(intervals)
    );
  }
}
