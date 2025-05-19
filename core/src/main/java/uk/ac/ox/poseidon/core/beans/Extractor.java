/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.beans;

import com.google.common.collect.Streams;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Extractor {

    private final Supplier<AddressBuilder> addressBuilderSupplier;
    private final boolean includeReadOnly;

    private static List<PropertyDescriptor> getPropertyDescriptors(final Object object) {
        try {
            return Arrays
                .stream(Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors())
                .filter(propertyDescriptor -> !propertyDescriptor.getName().equals("class"))
                .collect(Collectors.toList());
        } catch (final IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object invoke(
        final Object object,
        final Method method
    ) {
        try {
            return method.invoke(object);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<ExtractedParameter> getParameters(final Object object) {
        return getParameters(object, addressBuilderSupplier.get());
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<ExtractedParameter> getParameters(
        final Object object,
        final AddressBuilder addressBuilder
    ) {
        return Streams
            .stream(Optional.ofNullable(object))
            .filter(o ->
                // Exclude problematic types from search:
                // - Class objects have annotation getters that generate IllegalAccessException
                // on newer JVMs
                // - Path objects iterate on themselves, creating an infinite loop
                !(o instanceof Class || o instanceof Path)
            )
            .flatMap(o -> {
                if (o instanceof Map) {
                    return getParametersFromMap((Map<?, ?>) o, addressBuilder);
                } else if (o instanceof Iterable) {
                    return getParametersFromIterable((Iterable<?>) o, addressBuilder);
                } else {
                    return getParametersFromObject(o, addressBuilder);
                }
            });
    }

    private Stream<ExtractedParameter> getParametersFromObject(
        final Object o,
        final AddressBuilder addressBuilder
    ) {
        final List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(o);
        if (propertyDescriptors.isEmpty())
            return Stream.of(new ExtractedParameter(addressBuilder.get(), o));
        else
            return propertyDescriptors
                .stream()
                .filter(propertyDescriptor -> propertyDescriptor.getReadMethod() != null)
                .filter(propertyDescriptor ->
                    includeReadOnly || propertyDescriptor.getWriteMethod() != null
                )
                .flatMap(propertyDescriptor -> getParameters(
                    invoke(o, propertyDescriptor.getReadMethod()),
                    addressBuilder.add(propertyDescriptor.getName())
                ));
    }

    private Stream<ExtractedParameter> getParametersFromMap(
        final Map<?, ?> objectMap,
        final AddressBuilder address
    ) {
        return objectMap
            .entrySet()
            .stream()
            .flatMap(entry ->
                getParameters(
                    entry.getValue(),
                    address.addKey(entry.getKey().toString())
                )
            );
    }

    private Stream<ExtractedParameter> getParametersFromIterable(
        final Iterable<?> objects,
        final AddressBuilder address
    ) {
        return Streams
            .mapWithIndex(
                Streams.stream(objects),
                Map::entry
            )
            .flatMap(entry ->
                getParameters(
                    entry.getKey(),
                    address.addIndex(entry.getValue())
                )
            );
    }

    @Data
    public static class ExtractedParameter {
        private final String address;
        private final Object object;

        @Override
        public String toString() {
            return address + ": " + object;
        }
    }

}
