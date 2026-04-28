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
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MappedFactory<S extends Scope, C> extends RelativeScopeFactory<S, List<C>> {

    private Factory<S, C> factory;
    @Singular private SequencedMap<String, Factory<? super S, ? extends List<?>>> mappedProperties;

    public MappedFactory(
        final Factory<S, C> factory,
        final Map<String, Factory<? super S, ? extends List<?>>> mappedProperties
    ) {
        this(factory, new LinkedHashMap<>(mappedProperties));
    }

    @Override
    protected List<C> newInstance(final S scope) {

        checkNotNull(factory);
        checkNotNull(mappedProperties);
        checkState(!mappedProperties.isEmpty());

        final SequencedMap<String, ? extends List<?>> componentLists =
            mappedProperties.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get(scope),
                (a, b) -> b,
                LinkedHashMap::new
            ));

        final int targetSize = componentLists.values().iterator().next().size();
        checkState(
            componentLists.values().stream().map(List::size).allMatch(n -> n == targetSize),
            "All property value lists must be the same size."
        );

        final SequencedMap<String, Object> originalPropertyValues =
            mappedProperties
                .sequencedKeySet()
                .stream()
                .collect(
                    LinkedHashMap::new,
                    (originalValues, propertyName) ->
                        originalValues.put(propertyName, getProperty(propertyName)),
                    Map::putAll
                );

        try {
            return range(0, targetSize)
                .mapToObj(componentIndex -> {
                    componentLists.forEach((propertyName, components) ->
                        setProperty(
                            propertyName,
                            switch (components.get(componentIndex)) {
                                case null -> null;
                                case final Boolean b -> b;
                                case final Character c -> c;
                                case final Number n -> n;
                                case final String s -> s;
                                case final Factory<?, ?> f -> f;
                                case final Object o -> object(o);
                            }
                        ));
                    return factory.get(scope);
                })
                .toList();
        } finally {
            originalPropertyValues.reversed().forEach(this::setProperty);
        }
    }

    private Object getProperty(
        final String propertyName
    ) {
        try {
            return PropertyUtils.getProperty(factory, propertyName);
        } catch (
            final IllegalAccessException | InvocationTargetException |
                  NoSuchMethodException e
        ) {
            throw new RuntimeException(
                "Failed to read property '" + propertyName + "' on factory " + factory +
                    " (mapped properties: " + mappedPropertyKeys() + ")",
                e
            );
        }
    }

    private void setProperty(
        final String propertyName,
        final Object component
    ) {
        try {
            PropertyUtils.setProperty(
                factory,
                propertyName,
                component
            );
        } catch (
            final IllegalAccessException | InvocationTargetException |
                  NoSuchMethodException e
        ) {
            throw new RuntimeException(
                "Failed to set property '" + propertyName + "' on factory " + factory +
                    " (mapped properties: " + mappedPropertyKeys() + ")",
                e
            );
        }
    }

    private String mappedPropertyKeys() {
        return mappedProperties == null ? "<null>" : mappedProperties.keySet().toString();
    }

}
