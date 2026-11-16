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

import static java.beans.Introspector.getBeanInfo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Readable JavaBean properties, excluding those inherited from Object, cached
 * per class. Reading a property preserves its value, including containers and
 * nulls; it does not traverse the value.
 */
final class ReadableProperties {

  private static final ClassValue<List<PropertyDescriptor>> PROPERTIES =
    new ClassValue<>() {
      @Override
      protected List<PropertyDescriptor> computeValue(final Class<?> type) {
        try {
          return Arrays.stream(
            getBeanInfo(type, Object.class).getPropertyDescriptors()
          )
            .filter(property -> property.getReadMethod() != null)
            .toList();
        } catch (final IntrospectionException e) {
          throw new IllegalStateException(e);
        }
      }
    };

  private ReadableProperties() {}

  static List<PropertyDescriptor> of(final Class<?> type) {
    return PROPERTIES.get(type);
  }

  static Object read(final Object bean, final PropertyDescriptor property) {
    try {
      return property.getReadMethod().invoke(bean);
    } catch (final IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
        "Cannot read " + property.getName() + " on " + bean.getClass(),
        e
      );
    }
  }
}
