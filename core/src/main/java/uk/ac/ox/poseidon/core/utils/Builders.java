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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Calls Lombok builders' methods by name, for code that builds or copies
 * objects described by their property names: the YAML loader and scenario
 * derivation.
 */
public final class Builders {

  private Builders() {}

  /**
   * Calls the method of {@code target} named {@code name} that takes as many
   * arguments as given. Lombok's builders give each property one such method.
   *
   * @param target    the object to call the method on
   * @param name      the method's name
   * @param arguments the method's arguments
   * @return what the method returns
   * @throws IllegalArgumentException if {@code target} does not have exactly
   *                                  one such method, or the method throws
   */
  public static Object invoke(
    final Object target,
    final String name,
    final Object... arguments
  ) {
    final List<Method> methods = Arrays.stream(target.getClass().getMethods())
      // Lombok's builders override their parents' methods with narrower
      // return types, which leaves bridge methods of the same name
      .filter(method -> !method.isBridge())
      .filter(method -> method.getName().equals(name))
      .filter(method -> method.getParameterCount() == arguments.length)
      .toList();
    if (methods.size() != 1) {
      throw new IllegalArgumentException(
        "Expected one method " +
          name +
          " taking " +
          arguments.length +
          " arguments on " +
          target.getClass().getName() +
          ", found " +
          methods.size()
      );
    }
    final Method method = methods.getFirst();
    try {
      method.setAccessible(true);
      return method.invoke(target, arguments);
    } catch (final IllegalAccessException | InvocationTargetException e) {
      throw new IllegalArgumentException(
        "Cannot call " + name + " on " + target.getClass().getName(),
        e
      );
    }
  }
}
