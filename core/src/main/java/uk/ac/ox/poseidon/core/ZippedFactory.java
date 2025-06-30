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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.IntStream.range;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZippedFactory<T> extends GlobalScopeFactory<List<T>> {

    private Factory<T> targetFactory;
    private List<Factory<? extends List<?>>> sourceFactories;
    private List<String> mappedProperties;

    @Override
    protected List<T> newInstance(final Simulation simulation) {

        checkNotNull(sourceFactories);
        checkState(!sourceFactories.isEmpty());
        checkNotNull(mappedProperties);
        checkState(!mappedProperties.isEmpty());

        checkState(
            mappedProperties.size() == sourceFactories.size(),
            "There must be one mapped property per source factory."
        );

        final List<? extends List<?>> sourceComponents =
            sourceFactories
                .stream()
                .map(f -> f.get(simulation)).toList();

        checkState(!sourceComponents.getFirst().isEmpty());
        final int targetSize = sourceComponents.getFirst().size();

        checkState(
            sourceComponents.stream().map(List::size).allMatch(n -> n == targetSize),
            "All source factories must generate the same number of components."
        );

        final Factory<T> targetFactory = this.targetFactory;
        synchronized (targetFactory) {
            return range(0, targetSize).mapToObj(componentIndex -> {
                    range(0, mappedProperties.size()).forEach(propertyIndex -> {
                        setProperty(
                            targetFactory,
                            mappedProperties.get(propertyIndex),
                            sourceComponents.get(propertyIndex).get(componentIndex)
                        );
                    });
                    return targetFactory.get(simulation);
                })
                .toList();
        }
    }

    private void setProperty(
        final Factory<T> targetFactory,
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
            mappedProperties,
            sourceFactories.stream().map(f -> f.get(simulation)).toList(),
            targetFactory.makeKey(simulation)
        ).hashCode();
    }
}
