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
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static java.beans.Introspector.getBeanInfo;
import static java.util.stream.Collectors.toMap;

/**
 * A {@link Factory} whose effective scope is derived from its own {@link Factory}-typed fields
 * rather than fixed: it inspects those fields, finds their common (leaf) scope class, and caches
 * its produced object under that same key — so it inherits whatever scope its inputs resolve at,
 * falling back to {@link Scope#GLOBAL_SCOPE} if it has no {@link Factory}-typed fields. Prefer
 * this over {@link GlobalScopeFactory} for any factory with {@link Factory}-typed input fields: it
 * still ends up at global scope when its inputs do, but safely demotes to a narrower scope instead
 * of silently sharing stale state if an input ever isn't global.
 */
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

        final Map<AbstractFactory, Class> scopesByFactory =
            memberFactories().collect(toMap(
                Function.identity(),
                AbstractFactory::scopeClass
            ));

        if (scopesByFactory.isEmpty()) {
            return Scope.GLOBAL_SCOPE;
        }

        final Set<Class> scopeClasses = Set.copyOf(scopesByFactory.values());

        final List<Class> leafScopes =
            scopeClasses
                .stream()
                .filter(c ->
                    scopeClasses
                        .stream()
                        .noneMatch(d -> c != d && c.isAssignableFrom(d))
                )
                .toList();

        checkState(
            leafScopes.size() == 1,
            "More than one leaf scope classes found amongst member factories: %s",
            leafScopes
        );

        final Class leafScope = leafScopes.getFirst();
        return scopesByFactory
            .entrySet()
            .stream()
            .filter(e -> e.getValue().equals(leafScope))
            .findFirst()
            .map(Map.Entry::getKey)
            .map(af -> af.makeKey(scope))
            .orElseThrow();
    }

    @Override
    protected Class<S> scopeClass() {
        throw new UnsupportedOperationException(
            "Relative scope factories do not provide a scope class");
    }

    @SuppressWarnings("rawtypes")
    synchronized protected Stream<AbstractFactory> memberFactories() {
        return leafFactories(
            readMethods
                .get()
                .stream()
                .map(readMethod -> {
                    try {
                        return readMethod.invoke(this);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
        ).distinct();
    }

    @SuppressWarnings("rawtypes")
    private <T> Stream<AbstractFactory> leafFactories(final Stream<T> objects) {
        return objects.flatMap(o ->
            switch (o) {
                case final Map<?, ?> map -> leafFactories(map.values().stream());
                case final Collection<?> collection -> leafFactories(collection.stream());
                case final RelativeScopeFactory<?, ?> factory -> factory.memberFactories();
                case final AbstractFactory factory -> Stream.of(factory);
                case null, default -> Stream.empty();
            }
        );
    }

}
