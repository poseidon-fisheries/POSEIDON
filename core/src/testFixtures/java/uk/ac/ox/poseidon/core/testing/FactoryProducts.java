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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.google.common.primitives.Primitives;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import uk.ac.ox.poseidon.core.AbstractFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * Checks that every concrete factory of a package builds a product rather than
 * null. It builds each factory through its Lombok builder, with a mock for each
 * input (a mocked factory resolves to a mock of what it produces), and resolves
 * it in a scope.
 */
public final class FactoryProducts {

  private FactoryProducts() {}

  /**
   * @param packagePrefix the package, and its subpackages, whose factories to
   *                      check
   * @param scope         the scope to resolve every factory in, which must be
   *                      at least as narrow as the narrowest factory
   * @param excluded      the factories to leave out
   * @return one test per factory with a builder
   */
  public static Stream<DynamicTest> everyFactoryBuildsItsProduct(
    final String packagePrefix,
    final Scope scope,
    final Predicate<? super Class<?>> excluded
  ) throws IOException {
    final List<Class<?>> factories = ClassPath.from(
      FactoryProducts.class.getClassLoader()
    )
      .getTopLevelClassesRecursive(packagePrefix)
      .stream()
      .map(ClassPath.ClassInfo::load)
      .filter(AbstractFactory.class::isAssignableFrom)
      .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
      .filter(FactoryProducts::hasBuilder)
      .filter(clazz -> !excluded.test(clazz))
      .sorted(Comparator.comparing(Class::getName))
      .toList();
    assertThat(factories).isNotEmpty();
    return factories
      .stream()
      .map(clazz -> dynamicTest(clazz.getName(), () -> check(clazz, scope)));
  }

  private static boolean hasBuilder(final Class<?> clazz) {
    return Arrays.stream(clazz.getMethods()).anyMatch(
      method ->
        method.getName().equals("builder") &&
        Modifier.isStatic(method.getModifiers()) &&
        method.getParameterCount() == 0
    );
  }

  @SuppressWarnings("unchecked")
  private static void check(final Class<?> clazz, final Scope scope)
    throws Exception {
    final Method newBuilder = clazz.getMethod("builder");
    newBuilder.setAccessible(true);
    final Object builder = newBuilder.invoke(null);
    for (final Method method : builder.getClass().getMethods()) {
      if (
        method.getParameterCount() == 1 &&
        method.getDeclaringClass() != Object.class &&
        !method.getName().equals("self") &&
        !method.getName().equals("equals") &&
        !method.getName().startsWith("$fillValuesFrom")
      ) {
        method.setAccessible(true);
        method.invoke(
          builder,
          argumentOf(method.getGenericParameterTypes()[0])
        );
      }
    }
    final Method build = builder.getClass().getMethod("build");
    build.setAccessible(true);
    final Factory<Scope, ?> factory = (Factory<Scope, ?>) build.invoke(builder);

    assertThat(factory.get(scope)).isNotNull();
  }

  private static Object argumentOf(final Type type) {
    final Class<?> raw = rawClass(type);
    final Object argument;
    if (
      raw.isPrimitive() ||
      Primitives.isWrapperType(raw) ||
      raw == String.class ||
      raw.isEnum()
    ) {
      argument = FactoriesHelpers.argumentOf(Primitives.unwrap(raw));
    } else if (Factory.class.isAssignableFrom(raw)) {
      final Type produced = TypeToken.of(type)
        .resolveType(Factory.class.getTypeParameters()[1])
        .getType();
      argument = mock(mockable(raw), invocation ->
        invocation.getMethod().getName().equals("get")
          ? argumentOf(produced)
          : null
      );
    } else if (
      raw.isArray() ||
      raw.isAssignableFrom(java.util.List.class) ||
      raw.isAssignableFrom(java.util.Set.class) ||
      raw.isAssignableFrom(java.util.Map.class)
    ) {
      argument = FactoriesHelpers.argumentOf(raw);
    } else {
      argument = mock(mockable(raw), RETURNS_DEEP_STUBS);
    }
    return argument;
  }

  private static Class<?> mockable(final Class<?> type) {
    return type.isSealed() ? mockable(type.getPermittedSubclasses()[0]) : type;
  }

  private static Class<?> rawClass(final Type type) {
    final Class<?> raw;
    if (type instanceof final Class<?> clazz) {
      raw = clazz;
    } else if (type instanceof final ParameterizedType parameterized) {
      raw = rawClass(parameterized.getRawType());
    } else if (type instanceof final WildcardType wildcard) {
      raw = rawClass(wildcard.getUpperBounds()[0]);
    } else if (type instanceof final TypeVariable<?> variable) {
      raw = rawClass(variable.getBounds()[0]);
    } else {
      raw = Object.class;
    }
    return raw;
  }
}
