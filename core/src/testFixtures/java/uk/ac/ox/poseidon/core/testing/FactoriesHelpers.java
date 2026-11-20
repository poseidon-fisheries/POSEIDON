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

package uk.ac.ox.poseidon.core.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;

/**
 * Checks that every public static helper in the {@code Factories} classes of a
 * package returns an object rather than null. It runs each helper, so that PIT
 * can tell a helper whose result was replaced by null; it does not check that a
 * helper passes its arguments on in the right places, which a test of the
 * helper has to do.
 */
public final class FactoriesHelpers {

  private FactoriesHelpers() {}

  /**
   * @param packagePrefix the package, and its subpackages, whose
   *                      {@code Factories} classes to check
   * @return one test per public static helper, which calls it with mocks and
   *         default values for its arguments
   */
  public static Stream<DynamicTest> everyHelperBuildsItsResult(
    final String packagePrefix
  ) throws IOException {
    return everyHelperBuildsItsResult(packagePrefix, method -> false);
  }

  /**
   * @param packagePrefix the package, and its subpackages, whose
   *                      {@code Factories} classes to check
   * @param excluded      the helpers to leave out, because they read or parse
   *                      their arguments, so that none of the default values
   *                      suits them: a test of their own has to cover them
   * @return one test per public static helper not excluded, which calls it
   *         with mocks and default values for its arguments
   * @throws AssertionError if there is no helper to check, as when
   *                        {@code packagePrefix} names no package
   */
  public static Stream<DynamicTest> everyHelperBuildsItsResult(
    final String packagePrefix,
    final Predicate<? super Method> excluded
  ) throws IOException {
    final List<Method> helpers = ClassPath.from(
      FactoriesHelpers.class.getClassLoader()
    )
      .getTopLevelClassesRecursive(packagePrefix)
      .stream()
      .filter(info -> info.getSimpleName().equals("Factories"))
      .map(ClassPath.ClassInfo::load)
      .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
      .filter(method -> Modifier.isPublic(method.getModifiers()))
      .filter(method -> Modifier.isStatic(method.getModifiers()))
      .filter(method -> !excluded.test(method))
      .sorted(
        Comparator.comparing((Method method) ->
          method.getDeclaringClass().getName()
        ).thenComparing(Method::toGenericString)
      )
      .toList();
    assertThat(helpers)
      .as("Factories helpers under %s", packagePrefix)
      .isNotEmpty();
    return helpers
      .stream()
      .map(method ->
        dynamicTest(method.toGenericString(), () -> check(method))
      );
  }

  private static void check(final Method method) throws Exception {
    final Object result = method.invoke(
      null,
      Arrays.stream(method.getParameterTypes())
        .map(FactoriesHelpers::argumentOf)
        .toArray()
    );

    assertThat(result).isNotNull();
  }

  private static Object argumentOf(final Class<?> type) {
    final Object argument;
    if (type == boolean.class) {
      argument = false;
    } else if (type == char.class) {
      argument = 'x';
    } else if (type == byte.class) {
      argument = (byte) 1;
    } else if (type == short.class) {
      argument = (short) 1;
    } else if (type == int.class) {
      argument = 1;
    } else if (type == long.class) {
      argument = 1L;
    } else if (type == float.class) {
      argument = 1f;
    } else if (type == double.class) {
      argument = 1.0;
    } else if (type == String.class) {
      argument = "x";
    } else if (type == LocalDate.class) {
      argument = LocalDate.of(2026, 1, 1);
    } else if (type.isArray()) {
      argument = Array.newInstance(type.getComponentType(), 0);
    } else if (type.isEnum()) {
      argument = type.getEnumConstants()[0];
    } else if (emptyCollection(type) != null) {
      // A mocked collection copies as null, so that copying it fails
      argument = emptyCollection(type);
    } else if (type.isSealed()) {
      // A sealed type cannot be mocked, but one of its subtypes can
      argument = argumentOf(type.getPermittedSubclasses()[0]);
    } else {
      argument = mock(type);
    }
    return argument;
  }

  private static Object emptyCollection(final Class<?> type) {
    return type == Object.class
      ? null
      : Stream.<Object>of(
          ImmutableList.of(),
          ImmutableSet.of(),
          ImmutableMap.of()
        )
          .filter(type::isInstance)
          .findFirst()
          .orElse(null);
  }
}
