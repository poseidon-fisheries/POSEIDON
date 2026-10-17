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

package uk.ac.ox.poseidon.core.functions;

import java.util.function.Function;

/**
 * A value that reads a property of the context, such as a vessel, by following
 * a dotted path of getters, as {@link ObjectProperty} does for the thing
 * mapped. Built via a subclass of {@link ObjectPropertyValueFactory}, which
 * fixes the class the path starts from.
 *
 * @param <C> the type of the context read
 * @param <R> the type of the property; not checked
 */
public class ObjectPropertyValue<C, R> implements Function<C, R> {

  private final ObjectProperty<C, R> property;

  /**
   * @param rootClass    the class the path starts from
   * @param propertyPath the dotted path of properties to follow
   * @throws IllegalArgumentException if a property name along the path is not a
   *                                  Java identifier, or has no public getter
   *                                  on the declared type it is read from
   */
  ObjectPropertyValue(final Class<C> rootClass, final String propertyPath) {
    this.property = new ObjectProperty<>(rootClass, propertyPath);
  }

  /**
   * @throws NullPointerException if a property part way along the path is
   *                              {@code null}
   */
  @Override
  public R apply(final C context) {
    return property.read(context);
  }
}
