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
import uk.ac.ox.poseidon.core.utils.Literals;

/**
 * A scenario has one kind of part: a factory or a literal. So every builder
 * property of a factory is a factory, a literal (a primitive or boxed scalar, a
 * {@code String} or an enum), a list or a map, and the elements of a list and
 * the values of a map obey the same rule. The keys of a map are literals, and so
 * are the elements and keys of any list or map used as a key.
 */
class FactoryPropertiesTest {

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
  void aPlainBeanAsAMapKeyIsReported() {
    assertThat(offendingProperties(BeanKeyFactory.class)).containsExactly(
      BeanKeyFactory.class.getName() + ".beans"
    );
  }

  @Test
  void aFactoryAsAMapKeyIsReported() {
    assertThat(offendingProperties(FactoryKeyFactory.class)).containsExactly(
      FactoryKeyFactory.class.getName() + ".factories"
    );
  }

  @Test
  void aPlainBeanInAListKeyIsReported() {
    assertThat(offendingProperties(ListKeyFactory.class)).containsExactly(
      ListKeyFactory.class.getName() + ".beans"
    );
  }

  @Test
  void aPlainBeanKeyInAMapInAListIsReported() {
    assertThat(offendingProperties(MapInListFactory.class)).containsExactly(
      MapInListFactory.class.getName() + ".beans"
    );
  }

  @Test
  void aPlainBeanKeyInAMapInAMapIsReported() {
    assertThat(offendingProperties(MapInMapFactory.class)).containsExactly(
      MapInMapFactory.class.getName() + ".beans"
    );
  }

  @Test
  void aDeclaredObjectMapKeyIsReported() {
    assertThat(offendingProperties(ObjectKeyFactory.class)).containsExactly(
      ObjectKeyFactory.class.getName() + ".values"
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
      case final Class<?> clazz -> Literals.isScalarType(clazz) ||
        Factory.class.isAssignableFrom(clazz);
      case final ParameterizedType parameterized -> isCollection(
        parameterized.getRawType()
      )
        ? isFactoryOrLiteralCollection(parameterized)
        : isFactoryOrLiteral(parameterized.getRawType());
      case final WildcardType wildcard -> Arrays.stream(
        wildcard.getUpperBounds()
      ).allMatch(FactoryPropertiesTest::isFactoryOrLiteral);
      // A type parameter bounded only by Object holds a literal of any type,
      // which erasure hides: the factory checks it with Literals.isLiteral
      // when it is built
      case final TypeVariable<?> variable -> isBoundedOnlyByObject(variable) ||
        Arrays.stream(variable.getBounds()).allMatch(
          FactoryPropertiesTest::isFactoryOrLiteral
        );
      default -> false;
    };
  }

  /**
   * The elements of a list and the values of a map are a factory or a literal;
   * the keys of a map are literals.
   */
  private static boolean isFactoryOrLiteralCollection(
    final ParameterizedType collection
  ) {
    final Type[] arguments = collection.getActualTypeArguments();
    return (
      isFactoryOrLiteral(arguments[arguments.length - 1]) &&
      Arrays.stream(arguments, 0, arguments.length - 1).allMatch(
        FactoryPropertiesTest::isLiteral
      )
    );
  }

  /**
   * A scalar, or a list or map of literals, recursively: a factory is not a
   * literal.
   */
  private static boolean isLiteral(final Type type) {
    return switch (type) {
      case final Class<?> clazz -> Literals.isScalarType(clazz);
      case final ParameterizedType parameterized -> isCollection(
        parameterized.getRawType()
      )
        ? Arrays.stream(parameterized.getActualTypeArguments()).allMatch(
            FactoryPropertiesTest::isLiteral
          )
        : isLiteral(parameterized.getRawType());
      case final WildcardType wildcard -> Arrays.stream(
        wildcard.getUpperBounds()
      ).allMatch(FactoryPropertiesTest::isLiteral);
      case final TypeVariable<?> variable -> isBoundedOnlyByObject(variable) ||
        Arrays.stream(variable.getBounds()).allMatch(
          FactoryPropertiesTest::isLiteral
        );
      default -> false;
    };
  }

  private static boolean isBoundedOnlyByObject(final TypeVariable<?> variable) {
    return Arrays.equals(variable.getBounds(), new Type[] { Object.class });
  }

  /** A list, or a map: whose elements, or values, are checked in turn. */
  private static boolean isCollection(final Type raw) {
    return (
      raw instanceof Class<?> clazz &&
      (List.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz))
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

  public static final class BeanKeyFactory extends TestFactory {

    public Map<Bean, String> getBeans() {
      return null;
    }
  }

  public static final class FactoryKeyFactory extends TestFactory {

    public Map<Factory<Scope, Object>, String> getFactories() {
      return null;
    }
  }

  public static final class ListKeyFactory extends TestFactory {

    public Map<List<Bean>, String> getBeans() {
      return null;
    }
  }

  public static final class MapInListFactory extends TestFactory {

    public List<Map<Bean, String>> getBeans() {
      return null;
    }
  }

  public static final class MapInMapFactory extends TestFactory {

    public Map<String, Map<Bean, String>> getBeans() {
      return null;
    }
  }

  public static final class ObjectKeyFactory extends TestFactory {

    public Map<Object, String> getValues() {
      return null;
    }
  }

  public static final class FineFactory extends TestFactory {

    public Map<List<String>, Factory<Scope, Object>> getListKeyed() {
      return null;
    }

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
