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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for values and mappings that transform, combine, or look up other
 * values and mappings. A combinator, or a block that takes its input, has one
 * overload per shape: built from values, it gives a value; built from mappings,
 * a mapping. Both mean the same: the value is the mapping, with nothing to map.
 */
@SuppressWarnings("Convert2Diamond")
public class Factories {

  private Factories() {}

  /**
   * @param key factory for the mapping that reads the key from the thing
   *            mapped
   * @param map factory for the map to look the key up in
   * @return a {@link MappingFactory} for a {@link MapLookup} of the key in the
   *         resolved map
   * @see MapLookup
   */
  public static <S extends Scope, C, T, K, V> MapLookupFactory<
    S,
    C,
    T,
    K,
    V
  > mapLookup(
    final MappingFactory<? super S, ? super C, ? super T, ? extends K> key,
    final Factory<? super S, ? extends Map<? super K, ? extends V>> map
  ) {
    return new MapLookupFactory<S, C, T, K, V>(key, map);
  }

  /**
   * @param key factory for the value that is the key
   * @param map factory for the map to look the key up in
   * @return a {@link ValueFactory} for a {@link MapLookup} of the key in the
   *         resolved map, made a value by {@link MappingAsValue}
   * @see MapLookup
   */
  public static <S extends Scope, C, K, V> MappingAsValueFactory<
    S,
    C,
    V
  > mapLookup(
    final ValueFactory<? super S, ? super C, ? extends K> key,
    final Factory<? super S, ? extends Map<? super K, ? extends V>> map
  ) {
    return new MappingAsValueFactory<S, C, V>(
      new MapLookupFactory<S, C, Void, K, V>(
        new ValueAsMappingFactory<S, C, K>(key),
        map
      )
    );
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
   * @return a {@link ValueFactory} for a {@link CombinedDuration} of the
   *         resolved values, made a value by {@link MappingAsValue}
   * @see CombinedDuration
   */
  @SafeVarargs
  public static <S extends Scope, C> MappingAsValueFactory<
    S,
    C,
    Duration
  > combinedDuration(
    final ValueFactory<? super S, ? super C, ? extends Duration>... durations
  ) {
    return new MappingAsValueFactory<S, C, Duration>(
      new CombinedDurationFactory<S, C, Void>(
        Arrays.stream(durations)
          .<
            MappingFactory<
              ? super S,
              ? super C,
              ? super Void,
              ? extends Duration
            >
          >map(duration -> new ValueAsMappingFactory<S, C, Duration>(duration))
          .collect(toImmutableList())
      )
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
   * @return a {@link ValueFactory} for a {@link DefaultIfNull} wrapping the
   *         resolved delegate and default, made a value by
   *         {@link MappingAsValue}
   * @see DefaultIfNull
   */
  public static <S extends Scope, C, R> MappingAsValueFactory<
    S,
    C,
    R
  > defaultIfNull(
    final ValueFactory<? super S, ? super C, ? extends R> delegate,
    final Factory<? super S, ? extends R> defaultValue
  ) {
    return new MappingAsValueFactory<S, C, R>(
      new DefaultIfNullFactory<S, C, Void, R>(
        new ValueAsMappingFactory<S, C, R>(delegate),
        defaultValue
      )
    );
  }

  /**
   * @param delegate     the value to read
   * @param defaultValue the literal value substituted when {@code delegate} is
   *                     {@code null}
   * @return a {@link ValueFactory} for a {@link DefaultIfNull} wrapping the
   *         resolved delegate and the given default, made a value by
   *         {@link MappingAsValue}
   * @see DefaultIfNull
   */
  public static <S extends Scope, C, R> MappingAsValueFactory<
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
   * @return a {@link ValueFactory} for a {@link MultiStringKeyFromFunctions} of
   *         the resolved values, made a value by {@link MappingAsValue}
   * @see MultiStringKeyFromFunctions
   */
  @SafeVarargs
  public static <S extends Scope, C> MappingAsValueFactory<
    S,
    C,
    String
  > multiStringKeyFromFunctions(
    final ValueFactory<? super S, ? super C, ?>... functions
  ) {
    return new MappingAsValueFactory<S, C, String>(
      new MultiStringKeyFromFunctionsFactory<S, C, Void>(
        Arrays.stream(functions)
          .<MappingFactory<? super S, ? super C, ? super Void, ?>>map(
            function -> new ValueAsMappingFactory<S, C, Object>(function)
          )
          .collect(toImmutableList())
      )
    );
  }

  /**
   * @param number    the mapping that reads the number to place
   * @param intervals the non-overlapping intervals to place it among (see
   *                  {@link IntervalLabelFactory#interval})
   * @return a {@link MappingFactory} for an {@link IntervalLabel} of the number
   *         among the given intervals
   * @see IntervalLabel
   */
  public static <S extends Scope, C, T> IntervalLabelFactory<
    S,
    C,
    T
  > intervalLabel(
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends Double
    > number,
    final IntervalLabelFactory.Interval... intervals
  ) {
    return new IntervalLabelFactory<S, C, T>(
      number,
      ImmutableList.copyOf(intervals)
    );
  }

  /**
   * @param number    the value that is the number to place
   * @param intervals the non-overlapping intervals to place it among (see
   *                  {@link IntervalLabelFactory#interval})
   * @return a {@link ValueFactory} for an {@link IntervalLabel} of the number
   *         among the given intervals, made a value by {@link MappingAsValue}
   * @see IntervalLabel
   */
  public static <S extends Scope, C> MappingAsValueFactory<
    S,
    C,
    String
  > intervalLabel(
    final ValueFactory<? super S, ? super C, ? extends Double> number,
    final IntervalLabelFactory.Interval... intervals
  ) {
    return new MappingAsValueFactory<S, C, String>(
      new IntervalLabelFactory<S, C, Void>(
        new ValueAsMappingFactory<S, C, Double>(number),
        ImmutableList.copyOf(intervals)
      )
    );
  }
}
