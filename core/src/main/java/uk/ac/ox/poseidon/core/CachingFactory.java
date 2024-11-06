/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.beans.Introspector.getBeanInfo;

public abstract class CachingFactory<C> implements Factory<C> {

    protected abstract C newInstance(Simulation simulation);

    List<Object> makeKey(final Simulation simulation) {
        try {
            return Arrays
                .stream(getBeanInfo(getClass()).getPropertyDescriptors())
                .map(propertyDescriptor -> {
                    try {
                        return propertyDescriptor.getReadMethod().invoke(this);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(o ->
                    o instanceof CachingFactory<?>
                        ? ((CachingFactory<?>) o).get(simulation)
                        : o
                )
                .collect(Collectors.toList());
        } catch (final IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }
}
