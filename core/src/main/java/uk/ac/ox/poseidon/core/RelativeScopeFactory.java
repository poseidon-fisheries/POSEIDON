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

import static com.google.common.base.Preconditions.checkState;
import static java.beans.Introspector.getBeanInfo;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A {@link Factory} whose level is derived from its inputs rather than
 * declared: the narrowest level among them, or global if it has none. Its
 * inputs are the factories its bean properties hold that are typed in its own
 * scope variable ({@code Factory<? super S, ...>}, or a collection or map of
 * them); a child typed with a concrete scope class ({@code Factory<? super
 * VesselScope, ...>}) is held, not an input: the build cannot pass its own
 * scope to it, so it can only build it later, under a scope it makes, and its
 * level does not count. A property typed as a factory whose scope argument is
 * neither, such as a raw {@code Factory} or {@code Factory<?, ?>}, is refused,
 * since its type cannot tell an input from a held child.
 * <p>
 * A relative factory's bound is {@link Scope}: a class that needs a minimum
 * level, because its product holds mutable state of its own, its build
 * registers through the scope or has side effects on what its inputs give it,
 * declares its level with a level base class instead.
 * <p>
 * An input that is not an {@link AbstractFactory}, such as a lambda in a test,
 * has no level to read: the factory then takes the level of the scope it is
 * asked with, so that it never shares a product too widely.
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class RelativeScopeFactory<
  S extends Scope,
  P
> extends AbstractFactory<S, P> {

  private static final ClassValue<List<Method>> INPUT_READ_METHODS =
    new ClassValue<>() {
      @Override
      protected List<Method> computeValue(final Class<?> factoryClass) {
        return findInputReadMethods(factoryClass);
      }
    };

  // The narrowest level of the inputs that have one, here or in nested
  // relative inputs, or null if none has; and whether an input, here or
  // nested, has no level to read. Transient, so that neither is a property.
  // Both are computed on first use, never during construction.
  @SuppressWarnings("this-escape")
  private final transient Supplier<Class<? extends Scope>> inputLevel =
    Suppliers.memoize(() ->
      inputs()
        .map(RelativeScopeFactory::levelOf)
        .filter(Objects::nonNull)
        .reduce(RelativeScopeFactory::narrower)
        .orElse(null)
    );

  @SuppressWarnings("this-escape")
  private final transient Supplier<Boolean> hasUnlevelledInput =
    Suppliers.memoize(() ->
      inputs().anyMatch(input ->
        input instanceof final RelativeScopeFactory<?, ?> relative
          ? relative.hasUnlevelledInput.get()
          : !(input instanceof AbstractFactory)
      )
    );

  /**
   * @param factoryClass a relative factory class
   * @return the read methods of its properties that hold inputs
   * @throws IllegalStateException naming the class and the property, if a
   *                               property typed as a factory cannot be told
   *                               to be an input or a held child
   */
  public static List<Method> inputReadMethodsOf(
    final Class<? extends RelativeScopeFactory<?, ?>> factoryClass
  ) {
    return INPUT_READ_METHODS.get(factoryClass);
  }

  /**
   * @return the narrowest level of this factory's inputs, or global if it has
   *         none; the level of {@code scope} if an input has no level to read
   * @throws IllegalStateException if two inputs are at levels neither of
   *                               which is narrower than the other
   */
  @Override
  Class<? extends Scope> levelFor(final Scope scope) {
    final Class<? extends Scope> level = Objects.requireNonNullElse(
      inputLevel.get(),
      Scope.class
    );
    return hasUnlevelledInput.get() ? narrower(level, scope.getClass()) : level;
  }

  /**
   * @throws UnsupportedOperationException always: a relative factory's level
   *                                       is derived from its inputs (see
   *                                       {@link #levelFor}), not declared
   */
  @Override
  protected Class<S> scopeClass() {
    throw new UnsupportedOperationException(
      "Relative scope factories do not declare a level"
    );
  }

  private static Class<? extends Scope> levelOf(final Object input) {
    return switch (input) {
      case final RelativeScopeFactory<
        ?,
        ?
      > relative -> relative.inputLevel.get();
      case final AbstractFactory<?, ?> declared -> declared.scopeClass();
      default -> null;
    };
  }

  private Stream<Object> inputs() {
    return INPUT_READ_METHODS.get(getClass())
      .stream()
      .map(this::read)
      .flatMap(RelativeScopeFactory::factoriesIn);
  }

  private Object read(final Method readMethod) {
    try {
      return readMethod.invoke(this);
    } catch (final IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Stream<Object> factoriesIn(final Object value) {
    return switch (value) {
      case null -> Stream.empty();
      case final Map<?, ?> map -> map
        .values()
        .stream()
        .flatMap(RelativeScopeFactory::factoriesIn);
      case final Collection<?> collection -> collection
        .stream()
        .flatMap(RelativeScopeFactory::factoriesIn);
      default -> Stream.of(value);
    };
  }

  private static Class<? extends Scope> narrower(
    final Class<? extends Scope> a,
    final Class<? extends Scope> b
  ) {
    checkState(
      a.isAssignableFrom(b) || b.isAssignableFrom(a),
      "Inputs at levels %s and %s, neither of which is narrower than the other",
      a.getSimpleName(),
      b.getSimpleName()
    );
    return a.isAssignableFrom(b) ? b : a;
  }

  private static List<Method> findInputReadMethods(
    final Class<?> factoryClass
  ) {
    final TypeToken<?> classType = TypeToken.of(factoryClass);
    final Type scopeVariable = classType
      .resolveType(RelativeScopeFactory.class.getTypeParameters()[0])
      .getType();
    final PropertyDescriptor[] properties;
    try {
      properties = getBeanInfo(
        factoryClass,
        Object.class
      ).getPropertyDescriptors();
    } catch (final IntrospectionException e) {
      throw new IllegalStateException(e);
    }
    return Arrays.stream(properties)
      .filter(property -> property.getReadMethod() != null)
      .filter(property -> {
        final Type type = classType
          .resolveType(genericReturnType(property.getReadMethod()))
          .getType();
        return switch (kindOf(type, scopeVariable)) {
          case INPUT -> true;
          case HELD, NOT_A_FACTORY -> false;
          case UNTYPED -> throw new IllegalStateException(
            "Property " +
              property.getName() +
              " of " +
              factoryClass.getName() +
              " is typed as a factory whose scope argument is neither the " +
              "class's scope variable (an input) nor a concrete scope class " +
              "(held): " +
              type.getTypeName()
          );
        };
      })
      .map(PropertyDescriptor::getReadMethod)
      .toList();
  }

  // A public class inheriting a getter from a package-private one gets a
  // bridge method with an erased return type; the declared type is the
  // bridged method's.
  private static Type genericReturnType(final Method readMethod) {
    return Stream.<Class<?>>iterate(
      readMethod.getDeclaringClass(),
      Objects::nonNull,
      Class::getSuperclass
    )
      .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
      .filter(
        method ->
          method.getName().equals(readMethod.getName()) &&
          method.getParameterCount() == 0 &&
          !method.isBridge()
      )
      .findFirst()
      .orElse(readMethod)
      .getGenericReturnType();
  }

  private enum Kind {
    INPUT,
    HELD,
    UNTYPED,
    NOT_A_FACTORY
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static Kind kindOf(final Type type, final Type scopeVariable) {
    final TypeToken token = TypeToken.of(type);
    final Class<?> rawType = token.getRawType();
    final Kind kind;
    if (Factory.class.isAssignableFrom(rawType)) {
      kind = scopeArgumentKind(
        token.getSupertype(Factory.class).getType(),
        scopeVariable
      );
    } else if (Map.class.isAssignableFrom(rawType)) {
      kind = elementKind(token, Map.class, 1, scopeVariable);
    } else if (Collection.class.isAssignableFrom(rawType)) {
      kind = elementKind(token, Collection.class, 0, scopeVariable);
    } else {
      kind = Kind.NOT_A_FACTORY;
    }
    return kind;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static Kind elementKind(
    final TypeToken token,
    final Class<?> container,
    final int index,
    final Type scopeVariable
  ) {
    final Type containerType = token.getSupertype(container).getType();
    return containerType instanceof final ParameterizedType parameterized
      ? kindOf(
          upperBound(parameterized.getActualTypeArguments()[index]),
          scopeVariable
        )
      : Kind.NOT_A_FACTORY;
  }

  private static Type upperBound(final Type type) {
    return type instanceof final WildcardType wildcard
      ? wildcard.getUpperBounds()[0]
      : type;
  }

  private static Kind scopeArgumentKind(
    final Type factoryType,
    final Type scopeVariable
  ) {
    final Type scope = scopeIn(factoryType);
    final Kind kind;
    if (scope == null) {
      kind = Kind.UNTYPED;
    } else if (scope.equals(scopeVariable)) {
      kind = Kind.INPUT;
    } else if (
      scope instanceof final Class<?> scopeClass &&
      Scope.class.isAssignableFrom(scopeClass)
    ) {
      kind = Kind.HELD;
    } else {
      kind = Kind.UNTYPED;
    }
    return kind;
  }

  /**
   * @return the scope argument of a factory type, or {@code null} if it has
   *         none, or has a wildcard without a single lower bound
   */
  private static Type scopeIn(final Type factoryType) {
    final Type scope;
    if (!(factoryType instanceof final ParameterizedType parameterized)) {
      scope = null;
    } else if (
      parameterized.getActualTypeArguments()[0] instanceof
        final WildcardType wildcard
    ) {
      scope =
        wildcard.getLowerBounds().length == 1
          ? wildcard.getLowerBounds()[0]
          : null;
    } else {
      scope = parameterized.getActualTypeArguments()[0];
    }
    return scope;
  }
}
