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
import uk.ac.ox.poseidon.core.PerSimulationFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * General-purpose factories that don't belong to a more specific domain package: literal and
 * collection pass-throughs (e.g. {@link ObjectFactory}), simple combinations of other resolved
 * components (e.g. {@link PairFactory}), scope-changing wrappers, small standalone utilities like
 * a unique-id supplier, and calibration support for cloning a factory with varied property values.
 */
public class Factories {

    private Factories() {
    }

    /**
     * @param value the value to always resolve to
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} that always resolves to
     * {@code value}
     * @see ObjectFactory
     */
    public static <T> ObjectFactory<T> object(final T value) {
        return new ObjectFactory<>(value);
    }

    /**
     * @param values the component factories to resolve, in order
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link List} of the
     * resolved values
     * @see ListFactory
     */
    @SafeVarargs
    public static <S extends Scope, C> ListFactory<S, C> listOf(
        final Factory<S, ? extends C>... values
    ) {
        return new ListFactory<>(List.of(values));
    }

    /**
     * @param values the literal values to wrap
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} that always resolves to a
     * {@link List} of {@code values}
     * @see ObjectFactory
     */
    @SafeVarargs
    public static <T> ObjectFactory<List<T>> listOf(final T... values) {
        return new ObjectFactory<>(List.of(values));
    }

    /**
     * @param values the literal values to wrap
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} that always resolves to a
     * {@link List} of {@code values}
     * @see ObjectFactory
     */
    public static <T> ObjectFactory<List<T>> listOf(final Stream<T> values) {
        return new ObjectFactory<>(values.toList());
    }

    /**
     * @param values the literal values to wrap, deduplicated and order-preserved
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} that always resolves to a
     * {@link Set} of {@code values}
     * @see ObjectFactory
     */
    @SafeVarargs
    public static <T> ObjectFactory<Set<T>> setOf(final T... values) {
        // using LinkedHashSet to preserve insertion order and be consistent with SnakeYAML
        final LinkedHashSet<T> set = new LinkedHashSet<>(Arrays.asList(values));
        return new ObjectFactory<>(Collections.unmodifiableSet(set));
    }

    /**
     * @param values the literal values to wrap, deduplicated and order-preserved
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} that always resolves to a
     * {@link Set} of {@code values}
     * @see ObjectFactory
     */
    public static <T> ObjectFactory<Set<T>> setOf(final Stream<T> values) {
        // using LinkedHashSet to preserve insertion order and be consistent with SnakeYAML
        final LinkedHashSet<T> set = values.collect(Collectors.toCollection(LinkedHashSet::new));
        return new ObjectFactory<>(Collections.unmodifiableSet(set));
    }

    /**
     * @param first  factory for the first value
     * @param second factory for the second value
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link Pair} of the
     * resolved values
     * @see Pair
     */
    public static <S extends Scope, A, B> PairFactory<S, A, B> pair(
        final Factory<? super S, ? extends A> first,
        final Factory<? super S, ? extends B> second
    ) {
        return new PairFactory<>(first, second);
    }

    /**
     * @param prefix the prefix prepended to every generated id
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
     * {@link PrefixedIdSupplier} with that prefix
     * @see PrefixedIdSupplier
     */
    public static PrefixedIdSupplierFactory prefixedIdSupplier(final String prefix) {
        return new PrefixedIdSupplierFactory(prefix);
    }

    /**
     * @param delegate factory for the value to resolve once per simulation
     * @return a {@link PerSimulationFactory} wrapping {@code delegate}
     * @see uk.ac.ox.poseidon.core.PerSimulationFactory
     */
    public static <T> PerSimulationFactory<T> perSimulation(
        final Factory<? super SimulationScope, ? extends T> delegate
    ) {
        return new PerSimulationFactory<>(delegate);
    }

    /**
     * @param process factory for the steppable to register as a final process
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for the resolved steppable,
     * registered to run at simulation {@code finish()}
     * @see FinalProcessFactory
     */
    public static <C extends Steppable> FinalProcessFactory<C> finalProcess(
        final Factory<? super SimulationScope, C> process
    ) {
        return new FinalProcessFactory<>(process);
    }

    /**
     * @param consumer the bean-property setter to apply
     * @param values   the values to apply, one per clone in {@link #mappedFactory}
     * @return a {@link MappedProperty} pairing the setter with its values
     * @see MappedProperty
     */
    public static <S extends Scope, C, F extends Factory<S, C>, T> MappedProperty<F, T> mappedProperty(
        final BiConsumer<F, T> consumer,
        final List<T> values
    ) {
        return new MappedProperty<>(consumer, values);
    }

    /**
     * Clones {@code factory} once per value in {@code mappedProperties} (all of which must have
     * the same number of values), applying each mapped property's setter to its corresponding
     * clone, and collects the clones into a {@link ListFactory}. Used for calibration, to vary a
     * factory's property across a batch of otherwise-identical clones.
     *
     * @param factory          the factory to clone
     * @param mappedProperties the properties to vary across clones; all must have equal-length
     *                         value lists
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link List} of the
     * resolved clones
     * @see ListFactory
     */
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
            final Object clone = BeanUtils.cloneBean(factory);
            checkState(
                factory.getClass().isInstance(clone),
                "Clone of %s produced incompatible type: %s",
                factory.getClass(),
                clone.getClass()
            );
            return (F) clone;
        } catch (
            final IllegalAccessException | InstantiationException |
                  InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

}
