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

package uk.ac.ox.poseidon.examples;

import static java.beans.Introspector.getBeanInfo;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.reflect.ClassPath;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.AbstractFactory;
import uk.ac.ox.poseidon.core.Factory;

/**
 * Every child factory of a factory must be reachable through a bean property,
 * possibly inside a map or a collection: the YAML writer,
 * {@code RelativeScopeFactory}'s scope inference and anything else that walks
 * the factory graph find children that way, and a hidden child breaks all of
 * them. This test checks every {@link AbstractFactory} subclass in POSEIDON.
 */
class FactoryChildrenTest {

  @Test
  void everyFieldHoldingAFactoryIsABeanProperty() throws IOException {
    final List<String> hiddenChildren = new ArrayList<>();
    for (final Class<?> factoryClass : factoryClasses("uk.ac.ox.poseidon")) {
      final Set<String> readableProperties = readableProperties(factoryClass);
      for (final Field field : factoryClass.getDeclaredFields()) {
        final int modifiers = field.getModifiers();
        if (
          !Modifier.isStatic(modifiers) &&
          !Modifier.isTransient(modifiers) &&
          !field.isSynthetic() &&
          mentionsFactory(field.getGenericType()) &&
          !readableProperties.contains(field.getName())
        ) {
          hiddenChildren.add(factoryClass.getName() + "." + field.getName());
        }
      }
    }
    assertThat(hiddenChildren).isEmpty();
  }

  static List<Class<?>> factoryClasses(final String packagePrefix)
    throws IOException {
    return ClassPath.from(FactoryChildrenTest.class.getClassLoader())
      .getTopLevelClassesRecursive(packagePrefix)
      .stream()
      .flatMap(info -> withNestedClasses(info.load()).stream())
      .filter(AbstractFactory.class::isAssignableFrom)
      .toList();
  }

  private static List<Class<?>> withNestedClasses(final Class<?> clazz) {
    final List<Class<?>> classes = new ArrayList<>(List.of(clazz));
    Arrays.stream(clazz.getDeclaredClasses()).forEach(nested ->
      classes.addAll(withNestedClasses(nested))
    );
    return classes;
  }

  private static Set<String> readableProperties(final Class<?> clazz) {
    try {
      return Arrays.stream(getBeanInfo(clazz).getPropertyDescriptors())
        .filter(property -> property.getReadMethod() != null)
        .map(PropertyDescriptor::getName)
        .collect(Collectors.toSet());
    } catch (final IntrospectionException e) {
      throw new IllegalStateException(e);
    }
  }

  private static boolean mentionsFactory(final Type type) {
    return switch (type) {
      case final Class<?> clazz -> clazz.isArray()
        ? mentionsFactory(clazz.getComponentType())
        : Factory.class.isAssignableFrom(clazz);
      case final ParameterizedType parameterized -> mentionsFactory(
        parameterized.getRawType()
      ) ||
        Arrays.stream(parameterized.getActualTypeArguments()).anyMatch(
          FactoryChildrenTest::mentionsFactory
        );
      case final WildcardType wildcard -> Arrays.stream(
        wildcard.getUpperBounds()
      ).anyMatch(FactoryChildrenTest::mentionsFactory);
      case final GenericArrayType array -> mentionsFactory(
        array.getGenericComponentType()
      );
      default -> false;
    };
  }
}
