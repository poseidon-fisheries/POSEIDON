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

package uk.ac.ox.poseidon.core;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static java.beans.Introspector.getBeanInfo;
import static java.util.stream.Collectors.toSet;

@SuperBuilder
@NoArgsConstructor
public abstract class RelativeScopeFactory<S extends Scope, C> extends AbstractFactory<S, C> {

    private final transient Supplier<List<Method>> readMethods = Suppliers.memoize(() -> {
        final PropertyDescriptor[] props;
        try {
            props = getBeanInfo(this.getClass(), Object.class).getPropertyDescriptors();
        } catch (final IntrospectionException e) {
            throw new IllegalStateException(e);
        }
        return Arrays
            .stream(props)
            .map(PropertyDescriptor::getReadMethod)
            .filter(Objects::nonNull)
            .toList();
    });

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Object makeKey(final S scope) {
        final Collection<AbstractFactory> delegates = delegateFactories();
        final Set<? extends Class<?>> classes =
            delegates.stream().map(Object::getClass).collect(toSet());
        checkState(
            !classes.isEmpty(),
            "No delegate factories found for delegate scope factory %s",
            this
        );
        final List<? extends Class<?>> leafClasses =
            classes
                .stream()
                .filter(c ->
                    classes
                        .stream()
                        .noneMatch(d -> c != d && c.isAssignableFrom(d))
                )
                .toList();
        checkState(
            leafClasses.size() == 1,
            "More than one leaf factory classes found amongst delegates: %s",
            leafClasses
        );
        return delegates
            .stream()
            .filter(d -> leafClasses.getFirst().isAssignableFrom(d.getClass()))
            .findFirst()
            .map(f -> f.makeKey(scope))
            .orElseThrow();
    }

    @SuppressWarnings("rawtypes")
    synchronized protected List<AbstractFactory> delegateFactories() {
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
            .flatMap(o ->
                switch (o) {
                    case final Map<?, ?> map -> map.values().stream();
                    case final Collection<?> collection -> collection.stream();
                    default -> Stream.of(o);
                }
            )
            .filter(AbstractFactory.class::isInstance)
            .map(AbstractFactory.class::cast)
            .toList();
    }

}
