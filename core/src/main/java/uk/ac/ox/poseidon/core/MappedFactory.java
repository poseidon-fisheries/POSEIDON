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

package uk.ac.ox.poseidon.core;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.utils.ConstantFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.IntStream.range;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MappedFactory<S extends Scope, C> extends RelativeScopeFactory<S, List<C>> {

    private Factory<S, C> factory;
    @Singular private List<String> propertyNames;
    @Singular private List<Factory<? super S, ? extends List<?>>> componentListFactories;

    public MappedFactory(
        final Factory<S, C> factory,
        final String propertyName,
        final Factory<? super S, ? extends List<?>> componentListFactory
    ) {
        this.factory = factory;
        this.propertyNames = List.of(propertyName);
        this.componentListFactories = List.of(componentListFactory);
    }

    @Override
    protected List<C> newInstance(final S scope) {

        checkNotNull(componentListFactories);
        checkState(!componentListFactories.isEmpty());
        checkNotNull(propertyNames);
        checkState(!propertyNames.isEmpty());

        checkState(
            componentListFactories.size() == propertyNames.size(),
            "There must be as many are property value lists as there property names."
        );

        final List<? extends List<?>> componentLists = getComponentLists(scope);

        checkState(!componentLists.getFirst().isEmpty());
        final int targetSize = componentLists.getFirst().size();

        checkState(
            componentLists.stream().map(List::size).allMatch(n -> n == targetSize),
            "All property value lists must be the same size."
        );

        final Factory<S, C> factory = this.factory;
        synchronized (factory) {
            return range(0, targetSize).mapToObj(componentIndex -> {
                    range(0, propertyNames.size()).forEach(propertyIndex -> {
                        final Object o = componentLists.get(propertyIndex).get(componentIndex);
                        setProperty(
                            factory,
                            propertyNames.get(propertyIndex),
                            switch (o) {
                                case null -> null;
                                case final Boolean b -> b;
                                case final String s -> s;
                                case final Number n -> n;
                                case final Factory<?, ?> f -> f;
                                default -> new ConstantFactory<>(o);
                            }
                        );
                    });
                    return factory.get(scope);
                })
                .toList();
        }
    }

    private List<? extends List<?>> getComponentLists(final S scope) {
        return componentListFactories
            .stream()
            .map(f -> f.get(scope))
            .toList();
    }

    private void setProperty(
        final Factory<S, C> targetFactory,
        final String propertyName,
        final Object component
    ) {
        try {
            PropertyUtils.setProperty(
                targetFactory,
                propertyName,
                component
            );
        } catch (
            final IllegalAccessException | InvocationTargetException |
                  NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

}
