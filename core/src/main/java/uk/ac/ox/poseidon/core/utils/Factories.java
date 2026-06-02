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

package uk.ac.ox.poseidon.core.utils;

import org.apache.commons.beanutils.BeanUtils;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Factories {

    private Factories() {
    }

    public static <T> ObjectFactory<T> object(final T value) {
        return new ObjectFactory<>(value);
    }

    @SafeVarargs
    public static <S extends Scope, C> ListFactory<S, C> listOf(
        final Factory<S, ? extends C>... values
    ) {
        return new ListFactory<>(List.of(values));
    }

    @SafeVarargs
    public static <T> ObjectFactory<List<T>> listOf(final T... values) {
        return new ObjectFactory<>(List.of(values));
    }

    public static <T> ObjectFactory<List<T>> listOf(final Stream<T> values) {
        return new ObjectFactory<>(values.toList());
    }

    @SafeVarargs
    public static <T> ObjectFactory<Set<T>> setOf(final T... values) {
        // using LinkedHashSet to preserve insertion order and be consistent with SnakeYAML
        final LinkedHashSet<T> set = new LinkedHashSet<>(Arrays.asList(values));
        return new ObjectFactory<>(Collections.unmodifiableSet(set));
    }

    public static <T> ObjectFactory<Set<T>> setOf(final Stream<T> values) {
        // using LinkedHashSet to preserve insertion order and be consistent with SnakeYAML
        final LinkedHashSet<T> set = values.collect(Collectors.toCollection(LinkedHashSet::new));
        return new ObjectFactory<>(Collections.unmodifiableSet(set));
    }

    public static NumericIntervalToStringMapperFactory numericIntervalToStringMapper(
        final NumericIntervalToStringMapperFactory.Interval... intervals
    ) {
        return new NumericIntervalToStringMapperFactory(List.of(intervals));
    }

    public static <C extends Steppable> FinalProcessFactory<C> finalProcess(
        final Factory<? super SimulationScope, C> process
    ) {
        return new FinalProcessFactory<>(process);
    }

    public static <S extends Scope, C, F extends Factory<S, C>, T> MappedProperty<F, T> mappedProperty(
        final BiConsumer<F, T> consumer,
        final List<T> values
    ) {
        return new MappedProperty<>(consumer, values);
    }

    @SafeVarargs
    public static <S extends Scope, C, F extends Factory<S, C>> ListFactory<S, C> mappedFactory(
        final F factory,
        final MappedProperty<F, ?>... mappedProperties
    ) {
        checkNotNull(factory);
        checkNotNull(mappedProperties);
        checkArgument(mappedProperties.length > 0);
        final int size = mappedProperties[0].getValues().size();
        checkArgument(
            Arrays
                .stream(mappedProperties)
                .allMatch(p -> p.getValues().size() == size)
        );
        final List<Factory<? super S, ? extends C>> factories = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final F f = cloneFactory(factory);
            for (final MappedProperty<F, ?> mappedProperty : mappedProperties) {
                mappedProperty.accept(f, i);
            }
            factories.add(f);
        }
        return new ListFactory<>(factories);
    }

    @SuppressWarnings("unchecked")
    private static <F extends Factory<?, ?>> F cloneFactory(
        final F factory
    ) {
        try {
            return (F) BeanUtils.cloneBean(factory);
        } catch (
            final IllegalAccessException | InstantiationException |
                  InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

}
