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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.functions.IntervalLabelFactory;

/**
 * A scenario has one kind of part: a factory or a literal. So every builder
 * property of a factory is a factory, a literal (a primitive or boxed scalar, a
 * {@code String}, an enum, or a value type accepted as a leaf), a list or a
 * map, and the elements of a list and the values of a map obey the same rule.
 */
class FactoryPropertiesTest {

  /** Value types, other than scalars and enums, that are leaves. */
  private static final List<Class<?>> LEAVES = List.of(
    IntervalLabelFactory.Interval.class
  );

  @Test
  void everyBuilderPropertyOfAFactoryIsAFactoryOrALiteral() throws IOException {
    final List<String> offenders = new ArrayList<>();
    for (final Class<?> factoryClass : FactoryChildrenTest.factoryClasses(
      "uk.ac.ox.poseidon"
    )) {
      offenders.addAll(offendingProperties(factoryClass));
    }
    assertThat(offenders).isEmpty();
  }

  @Test
  void aPlainBeanPropertyIsReported() {
    assertThat(offendingProperties(PlainBeanFactory.class)).containsExactly(
      PlainBeanFactory.class.getName() + ".bean"
    );
  }

  @Test
  void aPlainBeanInAListIsReported() {
    assertThat(offendingProperties(ListOfBeansFactory.class)).containsExactly(
      ListOfBeansFactory.class.getName() + ".beans"
    );
  }

  @Test
  void aPlainBeanInAMapIsReported() {
    assertThat(offendingProperties(MapOfBeansFactory.class)).containsExactly(
      MapOfBeansFactory.class.getName() + ".beans"
    );
  }

  @Test
  void aPlainBeanInAListInAMapIsReported() {
    assertThat(offendingProperties(NestedBeansFactory.class)).containsExactly(
      NestedBeansFactory.class.getName() + ".beans"
    );
  }

  @Test
  void factoriesAndLiteralsAreAccepted() {
    assertThat(offendingProperties(FineFactory.class)).isEmpty();
  }

  private static List<String> offendingProperties(final Class<?> clazz) {
    try {
      return Arrays.stream(getBeanInfo(clazz).getPropertyDescriptors())
        .filter(property -> property.getReadMethod() != null)
        .filter(property -> !property.getName().equals("class"))
        .filter(property -> !isFactoryOrLiteral(propertyType(property)))
        .map(property -> clazz.getName() + "." + property.getName())
        .toList();
    } catch (final IntrospectionException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Type propertyType(final PropertyDescriptor property) {
    return property.getReadMethod().getGenericReturnType();
  }

  private static boolean isFactoryOrLiteral(final Type type) {
    return switch (type) {
      case final Class<?> clazz -> isLeaf(clazz) ||
        Factory.class.isAssignableFrom(clazz);
      case final ParameterizedType parameterized -> isCollection(
        parameterized.getRawType()
      )
        ? isFactoryOrLiteral(
            // The elements of a list, the values of a map
            parameterized.getActualTypeArguments()[
              parameterized.getActualTypeArguments().length - 1
            ]
          )
        : isFactoryOrLiteral(parameterized.getRawType());
      case final WildcardType wildcard -> Arrays.stream(
        wildcard.getUpperBounds()
      ).allMatch(FactoryPropertiesTest::isFactoryOrLiteral);
      // A type parameter bounded only by Object holds a literal of any type
      case final TypeVariable<?> variable -> Arrays.equals(
        variable.getBounds(),
        new Type[] { Object.class }
      ) ||
        Arrays.stream(variable.getBounds()).allMatch(
          FactoryPropertiesTest::isFactoryOrLiteral
        );
      default -> false;
    };
  }

  /** A list, or a map: whose elements, or values, are checked in turn. */
  private static boolean isCollection(final Type raw) {
    return (
      raw instanceof Class<?> clazz &&
      (List.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz))
    );
  }

  private static boolean isLeaf(final Class<?> clazz) {
    return (
      clazz.isPrimitive() ||
      clazz.isEnum() ||
      clazz == String.class ||
      clazz == Boolean.class ||
      clazz == Character.class ||
      Number.class.isAssignableFrom(clazz) ||
      LEAVES.stream().anyMatch(leaf -> leaf.isAssignableFrom(clazz))
    );
  }

  /** A plain bean, neither a factory nor a literal. */
  public static final class Bean {}

  public abstract static class TestFactory implements Factory<Scope, Object> {

    @Override
    public Object get(final Scope scope) {
      return null;
    }
  }

  public static final class PlainBeanFactory extends TestFactory {

    public Bean getBean() {
      return null;
    }
  }

  public static final class ListOfBeansFactory extends TestFactory {

    public List<Bean> getBeans() {
      return null;
    }
  }

  public static final class MapOfBeansFactory extends TestFactory {

    public Map<String, Bean> getBeans() {
      return null;
    }
  }

  public static final class NestedBeansFactory extends TestFactory {

    public Map<String, List<Bean>> getBeans() {
      return null;
    }
  }

  public static final class FineFactory extends TestFactory {

    public int getCount() {
      return 0;
    }

    public Map<String, List<Factory<Scope, Object>>> getFactories() {
      return null;
    }

    public List<String> getNames() {
      return null;
    }
  }
}
