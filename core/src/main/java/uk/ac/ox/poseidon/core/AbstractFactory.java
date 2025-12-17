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

package uk.ac.ox.poseidon.core;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.google.common.cache.CacheLoader.from;
import static java.beans.Introspector.getBeanInfo;
import static java.util.Comparator.comparing;

@SuperBuilder
public abstract class AbstractFactory<S, C> implements Factory<S, C> {

    // needs to be transient for SnakeYAML not to be confused
    // when there are no other properties to serialize
    private final transient LoadingCache<S, LoadingCache<Integer, C>> cache =
        CacheBuilder.newBuilder()
            .weakKeys()
            .build(from(scope ->
                CacheBuilder.newBuilder()
                    .build(from(() -> newInstance(scope)))
            ));

    @Getter
    @Accessors(makeFinal = true)
    private final Class<? extends S> scopeClass;

    protected AbstractFactory(final Class<? extends S> scopeClass) {this.scopeClass = scopeClass;}

    @Override
    public final C get(final S scope) {
        return cache
            .getUnchecked(scope)
            .getUnchecked(makeKey(scope));
    }

    private final transient Supplier<List<Method>> readMethods =
        Suppliers.memoize(() -> readMethods(this));

    public static List<Method> readMethods(final Object object) {
        final PropertyDescriptor[] props;
        try {
            props = getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors();
        } catch (final IntrospectionException e) {
            throw new RuntimeException(e);
        }
        Arrays.sort(props, comparing(PropertyDescriptor::getName));
        return Arrays
            .stream(props)
            .map(PropertyDescriptor::getReadMethod)
            .filter(Objects::nonNull)
            .toList();
    }

    protected abstract C newInstance(S scope);

    public final int makeKey(final S scope) {
        synchronized (this) {
            return readMethods
                .get()
                .stream()
                .map(readMethod -> {
                    try {
                        return readMethod.invoke(this);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(o ->
                    switch (o) {
                        case null -> null;
                        // noinspection rawtypes
                        case final Factory factory -> // noinspection unchecked
                            factory.get(factory.getScopeClass().cast(scope));
                        default -> o;
                    }
                )
                .toList()
                .hashCode();
        }
    }
}
