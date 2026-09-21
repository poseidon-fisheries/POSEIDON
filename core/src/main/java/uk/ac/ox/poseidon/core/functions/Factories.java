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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static uk.ac.ox.poseidon.core.utils.Factories.object;

/**
 * Factories for {@link Function}s that transform, combine, or look up values.
 */
public class Factories {
    private Factories() {}

    /**
     * @param map factory for the map to look values up in
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link MapValueExtractor} over the resolved map
     * @see MapValueExtractor
     */
    public static <S extends Scope, K, V> MapValueExtractorFactory<S, K, V> mapValueExtractor(
        final Factory<? super S, ? extends Map<? super K, ? extends V>> map
    ) {
        return new MapValueExtractorFactory<>(map);
    }

    /**
     * @param map factory for the map to look the value up in
     * @param key the fixed key to look up
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for the value found under
     * {@code key} in the resolved map
     * @see MapEntryFactory
     */
    public static <S extends Scope, K, V> MapEntryFactory<S, K, V> mapEntry(
        final Factory<? super S, ? extends Map<? super K, ? extends V>> map,
        final K key
    ) {
        return new MapEntryFactory<>(map, key);
    }

    /**
     * @param function1 the function applied first
     * @param function2 the function applied to {@code function1}'s result
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link ComposedFunction} of the two resolved functions
     * @see ComposedFunction
     */
    public static <S extends Scope, T1, T2, R> ComposedFunctionFactory<S, T1, T2, R> composedFunction(
        final Factory<? super S, ? extends Function<? super T1, ? extends T2>> function1,
        final Factory<? super S, ? extends Function<? super T2, ? extends R>> function2
    ) {
        return new ComposedFunctionFactory<>(function1, function2);
    }

    /**
     * @param durations the duration-producing functions to sum
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link CombinedDuration} of the resolved functions
     * @see CombinedDuration
     */
    @SafeVarargs
    public static <S extends Scope, T> CombinedDurationFactory<S, T> combinedDuration(
        final Factory<? super S, ? extends Function<? super T, ? extends Duration>>... durations
    ) {
        return new CombinedDurationFactory<S, T>(List.of(durations));
    }

    /**
     * @param delegate     the function to delegate to
     * @param defaultValue factory for the value substituted when {@code delegate} returns
     *                     {@code null}
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link DefaultIfNull}
     * wrapping the resolved delegate and default
     * @see DefaultIfNull
     */
    public static <S extends Scope, T, R> DefaultIfNullFactory<S, T, R> defaultIfNull(
        final Factory<? super S, ? extends Function<? super T, ? extends R>> delegate,
        final Factory<? super S, ? extends R> defaultValue
    ) {
        return new DefaultIfNullFactory<>(delegate, defaultValue);
    }

    /**
     * @param delegate     the function to delegate to
     * @param defaultValue the literal value substituted when {@code delegate} returns
     *                     {@code null}
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link DefaultIfNull}
     * wrapping the resolved delegate and the given default
     * @see DefaultIfNull
     */
    public static <S extends Scope, T, R> DefaultIfNullFactory<S, T, R> defaultIfNull(
        final Factory<? super S, ? extends Function<? super T, ? extends R>> delegate,
        final R defaultValue
    ) {
        return defaultIfNull(delegate, object(defaultValue));
    }

    /**
     * @param functions the functions to apply to the same input and join into one key
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link MultiStringKeyFromFunctions} of the resolved functions
     * @see MultiStringKeyFromFunctions
     */
    @SafeVarargs
    public static <S extends Scope, T> MultiStringKeyFromFunctionsFactory<S, T> multiStringKeyFromFunctions(
        final Factory<? super S, ? extends Function<? super T, ?>>... functions
    ) {
        // noinspection Convert2Diamond
        return new MultiStringKeyFromFunctionsFactory<S, T>(Arrays.asList(functions));
    }

    /**
     * @param intervals the non-overlapping intervals to map numbers into (see
     *                  {@link NumericIntervalToStringMapperFactory#interval})
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
     * {@link NumericIntervalMapper} over the given intervals
     * @see NumericIntervalMapper
     */
    public static NumericIntervalToStringMapperFactory numericIntervalToStringMapper(
        final NumericIntervalToStringMapperFactory.Interval... intervals
    ) {
        return new NumericIntervalToStringMapperFactory(List.of(intervals));
    }

}
