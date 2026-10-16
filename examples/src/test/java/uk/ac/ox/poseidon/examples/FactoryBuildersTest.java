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
import static uk.ac.ox.poseidon.examples.FactoryChildrenTest.factoryClasses;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Every concrete factory class has a static {@code builder()} and a
 * {@code toBuilder()}: the scenario loader builds factories through their
 * builders, and a changed copy of a factory is made from its
 * {@code toBuilder()}.
 */
class FactoryBuildersTest {

  @Test
  void everyConcreteFactoryHasABuilder() throws IOException {
    assertThat(
      factoryClasses("uk.ac.ox.poseidon")
        .stream()
        .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
        .filter(clazz -> !hasBuilders(clazz))
        .map(Class::getName)
    ).isEmpty();
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
