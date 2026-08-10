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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static uk.ac.ox.poseidon.core.utils.Factories.object;

public class Factories {
    private Factories() {}

    public static <S extends Scope, K, V> MapValueExtractorFactory<S, K, V> mapValueExtractor(
        final Factory<? super S, ? extends Map<? super K, ? extends V>> map
    ) {
        return new MapValueExtractorFactory<>(map);
    }

    public static <S extends Scope, K, V> MapEntryFactory<S, K, V> mapEntry(
        final Factory<? super S, ? extends Map<? super K, ? extends V>> map,
        final K key
    ) {
        return new MapEntryFactory<>(map, key);
    }

    public static <S extends Scope, T1, T2, R> ComposedFunctionFactory<S, T1, T2, R> composedFunction(
        final Factory<? super S, ? extends Function<? super T1, ? extends T2>> function1,
        final Factory<? super S, ? extends Function<? super T2, ? extends R>> function2
    ) {
        return new ComposedFunctionFactory<>(function1, function2);
    }

    public static <S extends Scope, T, R> DefaultIfNullFactory<S, T, R> defaultIfNull(
        final Factory<? super S, ? extends Function<? super T, ? extends R>> delegate,
        final Factory<? super S, ? extends R> defaultValue
    ) {
        return new DefaultIfNullFactory<>(delegate, defaultValue);
    }

    public static <S extends Scope, T, R> DefaultIfNullFactory<S, T, R> defaultIfNull(
        final Factory<? super S, ? extends Function<? super T, ? extends R>> delegate,
        final R defaultValue
    ) {
        return defaultIfNull(delegate, object(defaultValue));
    }

    @SafeVarargs
    public static <S extends Scope, T> MultiStringKeyFromFunctionsFactory<S, T> multiStringKeyFromFunctions(
        final Factory<? super S, ? extends Function<? super T, ?>>... functions
    ) {
        // noinspection Convert2Diamond
        return new MultiStringKeyFromFunctionsFactory<S, T>(Arrays.asList(functions));
    }

    public static NumericIntervalToStringMapperFactory numericIntervalToStringMapper(
        final NumericIntervalToStringMapperFactory.Interval... intervals
    ) {
        return new NumericIntervalToStringMapperFactory(List.of(intervals));
    }

}
