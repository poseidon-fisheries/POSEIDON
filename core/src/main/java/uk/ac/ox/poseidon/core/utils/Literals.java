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

package uk.ac.ox.poseidon.core.utils;

import com.google.common.primitives.Primitives;
import java.util.List;
import java.util.Map;

/**
 * What a scenario may hold besides factories: a scalar, a {@code String}, an
 * enum, or a list or map of literals, nested to any depth. Factories that hold
 * a value typed by a type parameter check it with {@link #isLiteral}, since
 * erasure hides what such a field holds from a static check.
 */
public final class Literals {

  private Literals() {}

  /**
   * @param clazz a class
   * @return whether instances of {@code clazz} are scalar literals: a
   *         primitive, a primitive wrapper, a {@code String} or an enum
   */
  public static boolean isScalarType(final Class<?> clazz) {
    return (
      clazz.isPrimitive() ||
      Primitives.isWrapperType(clazz) ||
      clazz == String.class ||
      clazz.isEnum()
    );
  }

  /**
   * @param value any object, possibly {@code null}
   * @return whether {@code value} is a literal: a scalar, a {@code String}, an
   *         enum, or a list or map whose elements, keys and values are all
   *         literals. A {@code null}, a factory and a set are not.
   */
  public static boolean isLiteral(final Object value) {
    return switch (value) {
      case null -> false;
      case final List<?> list -> list.stream().allMatch(Literals::isLiteral);
      case final Map<?, ?> map -> map
        .entrySet()
        .stream()
        .allMatch(
          entry -> isLiteral(entry.getKey()) && isLiteral(entry.getValue())
        );
      // An enum constant with a body is of an anonymous subclass of its enum
      case Enum<?> _ -> true;
      default -> isScalarType(value.getClass());
    };
  }

  /**
   * @param value what a factory is to hold
   * @param name  the factory property that holds it
   * @return {@code value}
   * @throws IllegalArgumentException if {@code value} is not a literal
   */
  public static <T> T requireLiteral(final T value, final String name) {
    if (!isLiteral(value)) {
      throw new IllegalArgumentException(
        String.format("%s must be a literal, not %s.", name, value)
      );
    }
    return value;
  }
}
