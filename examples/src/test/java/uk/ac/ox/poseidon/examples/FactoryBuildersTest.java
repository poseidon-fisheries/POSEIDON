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

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.examples.FactoryChildrenTest.nodeClasses;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

/**
 * Every concrete factory class has a static {@code builder()} and a
 * {@code toBuilder()}: the scenario loader builds factories through their
 * builders, and a changed copy of a factory is made from its
 * {@code toBuilder()}. The properties the scenario writer writes, read through
 * the factory's getters, are the ones its builder sets, so that a written
 * scenario loads back whole.
 */
class FactoryBuildersTest {

  @Test
  void everyConcreteNodeHasABuilder() throws IOException {
    assertThat(
      nodeClasses("uk.ac.ox.poseidon")
        .stream()
        .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
        .filter(clazz -> !hasBuilders(clazz))
        .map(Class::getName)
    ).isEmpty();
  }

  @Test
  void everyWrittenPropertyIsOneTheBuilderSets() throws IOException {
    final PropertyUtils propertyUtils = new PropertyUtils();
    propertyUtils.setAllowReadOnlyProperties(true);
    assertThat(
      nodeClasses("uk.ac.ox.poseidon")
        .stream()
        .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
        .filter(clazz -> {
          final Set<String> written = propertyUtils
            .getProperties(clazz)
            .stream()
            .map(Property::getName)
            .collect(Collectors.toCollection(TreeSet::new));
          return !written.equals(builderProperties(clazz));
        })
        .map(Class::getName)
    ).isEmpty();
  }

  private static Set<String> builderProperties(final Class<?> clazz) {
    final Set<String> properties = new TreeSet<>();
    try {
      for (
        Class<?> builderClass = clazz.getMethod("builder").getReturnType();
        builderClass != Object.class;
        builderClass = builderClass.getSuperclass()
      ) {
        Arrays.stream(builderClass.getDeclaredFields())
          .filter(field -> !Modifier.isStatic(field.getModifiers()))
          .map(Field::getName)
          // A field with a default is held as name$value and name$set
          .filter(name -> !name.endsWith("$set"))
          .map(name -> name.replace("$value", ""))
          .forEach(properties::add);
      }
    } catch (final NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
    return properties;
  }

  private static boolean hasBuilders(final Class<?> clazz) {
    final Method[] methods = clazz.getDeclaredMethods();
    return (
      Arrays.stream(methods).anyMatch(
        method ->
          method.getName().equals("builder") &&
          Modifier.isStatic(method.getModifiers())
      ) &&
      Arrays.stream(methods).anyMatch(method ->
        method.getName().equals("toBuilder")
      )
    );
  }
}
