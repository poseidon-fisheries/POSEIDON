/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.utility;

import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Just a way to link a class to its constructor map Created by carrknight on 5/29/15.
 */
@SuppressWarnings({"unchecked", "RedundantSuppression"})
public class AlgorithmFactories {

    public static final Map<Class<?>, Map<String, ? extends Supplier<?
        extends AlgorithmFactory<?>>>>
        CONSTRUCTOR_MAP = new HashMap<>();

    private static final Map<Class<?>, Map<Class<? extends AlgorithmFactory<?>>, String>>
        NAMES_MAP =
        new HashMap<>();

    private static void addFactories(final Factories<?> factories) {
        CONSTRUCTOR_MAP.put(factories.getClassObject(), factories.getConstructors());
        NAMES_MAP.put(factories.getClassObject(), factories.getNames());
    }

    /**
     * look up for any algorithm factory with a specific name, returning the first it finds
     *
     * @param name the name
     * @return the factory; or throws an exception if there isn't any!
     */
    public static AlgorithmFactory<?> constructorLookup(final String name) {
        return getFirstValueForKey(CONSTRUCTOR_MAP, name)
            .orElseThrow(() ->
                new RuntimeException("failed to find constructor named: " + name)
            )
            .get();
    }

    private static <V> Optional<? extends V> getFirstValueForKey(
        final Map<?, ? extends Map<?, ? extends V>> mapOfMaps,
        final Object key
    ) {
        return mapOfMaps
            .values()
            .stream()
            .filter(map -> map.containsKey(key))
            .map(map -> map.get(key))
            .findFirst();
    }

    /**
     * look up the name of the algorithm factory that has this class
     *
     * @param factoryClass the class for which to find the name
     * @return the factory or throws an exception if there isn't any!
     */
    public static String nameLookup(final Class<?> factoryClass) {
        return getFirstValueForKey(NAMES_MAP, factoryClass)
            .orElseThrow(() ->
                new RuntimeException("failed to find factory name for class " + factoryClass)
            );
    }

    /**
     * returns a stream with all the factories available in the constructor Maps
     */
    public static Stream<FactorySupplier> getAlgorithmFactoriesAsSuppliers() {
        return NAMES_MAP
            .entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().entrySet().stream())
            .map(entry -> new BasicFactorySupplier<>(entry.getKey(), entry.getValue()));
    }

    public static <T> Map<String, Supplier<AlgorithmFactory<? extends T>>> getConstructors(final Class<T> classObject) {
        return (Map<String, Supplier<AlgorithmFactory<? extends T>>>) CONSTRUCTOR_MAP.get(
            classObject);
    }
}
