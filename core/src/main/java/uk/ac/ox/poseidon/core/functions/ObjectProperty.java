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

import com.google.common.collect.ImmutableList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Throwables.throwIfUnchecked;

/**
 * Reads a property of an object by following a dotted path of getters: {@code "gear.code"} reads
 * {@code getGear().getCode()}, and {@code "active"} reads {@code isActive()} if it returns a
 * {@code boolean}. The getters are looked up once, when this is built, from the declared return
 * types along the path, so a wrong path fails when the scenario is built rather than when first
 * read. The last property may be {@code null}; a {@code null} part way along the path throws.
 * Built via a subclass of {@link ObjectPropertyFactory}, which fixes the class the path starts
 * from.
 *
 * @param <T> the type of the object read
 * @param <R> the type of the property; not checked
 */
public class ObjectProperty<T, R> implements Function<T, R> {

    private final String propertyPath;
    private final List<String> propertyNames;
    private final List<MethodHandle> getters;

    /**
     * @param rootClass    the class the path starts from
     * @param propertyPath the dotted path of properties to follow
     * @throws IllegalArgumentException if a property along the path has no public getter on the
     *                                  declared type it is read from
     */
    ObjectProperty(
        final Class<T> rootClass,
        final String propertyPath
    ) {
        this.propertyPath = propertyPath;
        this.propertyNames = ImmutableList.copyOf(propertyPath.split("\\."));
        final ImmutableList.Builder<MethodHandle> builder = ImmutableList.builder();
        Class<?> currentClass = rootClass;
        for (final String propertyName : propertyNames) {
            final Method getter = findGetter(currentClass, propertyName);
            try {
                builder.add(MethodHandles.publicLookup().unreflect(getter));
            } catch (final IllegalAccessException e) {
                throw new IllegalArgumentException(
                    "Getter %s is not accessible.".formatted(getter), e
                );
            }
            currentClass = getter.getReturnType();
        }
        this.getters = builder.build();
    }

    private static Method findGetter(
        final Class<?> type,
        final String propertyName
    ) {
        final String suffix =
            Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            return type.getMethod("get" + suffix);
        } catch (final NoSuchMethodException e) {
            try {
                final Method getter = type.getMethod("is" + suffix);
                if (getter.getReturnType() == boolean.class) return getter;
            } catch (final NoSuchMethodException ignored) {
                // reported below
            }
        }
        throw new IllegalArgumentException(
            "No getter for property %s on %s.".formatted(propertyName, type.getName())
        );
    }

    /**
     * @throws NullPointerException if a property part way along the path is {@code null}
     */
    @Override
    @SuppressWarnings("unchecked")
    public R apply(final T object) {
        Object value = object;
        for (int i = 0; i < getters.size(); i++) {
            if (value == null) {
                throw new NullPointerException(
                    "%s is null in property path %s.".formatted(
                        i == 0 ? "The object read" : propertyNames.get(i - 1),
                        propertyPath
                    )
                );
            }
            try {
                value = getters.get(i).invoke(value);
            } catch (final Throwable throwable) {
                throwIfUnchecked(throwable);
                throw new IllegalStateException(throwable);
            }
        }
        return (R) value;
    }
}
