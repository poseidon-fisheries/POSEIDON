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

import lombok.RequiredArgsConstructor;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.beans.Introspector.getBeanInfo;
import static java.util.Comparator.comparing;

@RequiredArgsConstructor
public class PropertyValuesExtractor {

    private final Object object;

    public List<Object> propertyValues() {
        synchronized (object) {
            try {
                final PropertyDescriptor[] props =
                    getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors();
                Arrays.sort(props, comparing(PropertyDescriptor::getName));
                return Arrays
                    .stream(props)
                    .map(PropertyDescriptor::getReadMethod)
                    .filter(Objects::nonNull)
                    .map(readMethod -> {
                        try {
                            return readMethod.invoke(object);
                        } catch (final IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
            } catch (final IntrospectionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
