/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.common.core;

import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.api.GenericComponentFactory;

import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;

public class BasicFactorySupplier<T extends GenericComponentFactory<?, ?>>
    implements FactorySupplier {

    private final Class<? extends T> factoryClass;
    private final String factoryName;

    public BasicFactorySupplier(
        final Class<? extends T> factoryClass
    ) {
        this(factoryClass, makeFactoryName(factoryClass));
    }

    public BasicFactorySupplier(
        final Class<? extends T> factoryClass,
        final String factoryName
    ) {
        this.factoryClass = factoryClass;
        this.factoryName = factoryName;
    }

    public static String makeFactoryName(final String className) {
        final String[] words =
            splitByCharacterTypeCamelCase(
                className.replaceAll("Factory", "")
            );
        return Stream
            .concat(
                Stream.of(words[0]),
                stream(words).skip(1).map(word -> word.toLowerCase(ENGLISH))
            )
            .collect(joining(" "));
    }

    public static String makeFactoryName(final Class<? extends GenericComponentFactory<?, ?>> classObject) {
        return makeFactoryName(classObject.getSimpleName());
    }

    @Override
    public String getFactoryName() {
        return factoryName;
    }

    @Override
    public Class<? extends GenericComponentFactory<?, ?>> getFactoryClass() {
        return factoryClass;
    }

    @Override
    public GenericComponentFactory<?, ?> get() {
        try {
            return factoryClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
