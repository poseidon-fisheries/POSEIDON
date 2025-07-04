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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.poseidon.core.utils.ConstantFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.IntStream.range;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MappedFactory<C> extends GlobalScopeFactory<List<C>> {

    private Factory<C> factory;
    private List<String> propertyNames;
    private List<Factory<? extends List<?>>> componentListFactories;

    public MappedFactory(
        final Factory<C> factory,
        final String propertyName,
        final Factory<? extends List<?>> componentListFactory
    ) {
        this.factory = factory;
        this.propertyNames = List.of(propertyName);
        this.componentListFactories = List.of(componentListFactory);
    }

    @Override
    protected List<C> newInstance(final Simulation simulation) {

        checkNotNull(componentListFactories);
        checkState(!componentListFactories.isEmpty());
        checkNotNull(propertyNames);
        checkState(!propertyNames.isEmpty());

        checkState(
            componentListFactories.size() == propertyNames.size(),
            "There must be as many are property value lists as there property names."
        );

        final List<? extends List<?>> componentLists = getComponentLists(simulation);

        checkState(!componentLists.getFirst().isEmpty());
        final int targetSize = componentLists.getFirst().size();

        checkState(
            componentLists.stream().map(List::size).allMatch(n -> n == targetSize),
            "All property value lists must be the same size."
        );

        final Factory<C> factory = this.factory;
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
                                case final Factory<?> f -> f;
                                case final AgentScopeFactory<?, ?> asf -> asf;
                                default -> new ConstantFactory<>(o);
                            }
                        );
                    });
                    return factory.get(simulation);
                })
                .toList();
        }
    }

    private List<? extends List<?>> getComponentLists(final Simulation simulation) {
        return componentListFactories
            .stream()
            .map(f -> f.get(simulation))
            .toList();
    }

    private void setProperty(
        final Factory<C> targetFactory,
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

    @Override
    int makeKey(final Simulation simulation) {
        // We override makeKey here because we want to use the properties of the target
        // factory as part of our own key instead of the object that it would instantiate
        // (or more likely die trying) if its get method gets called.
        return List.of(
            propertyNames,
            getComponentLists(simulation),
            factory.makeKey(simulation)
        ).hashCode();
    }
}
