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

package uk.ac.ox.poseidon.agents.travel;

import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.functions.ObjectProperty;

/**
 * A mapping that reads a property of a route: the route is read from the thing
 * mapped, under the context, and its property by following a dotted path of
 * getters, as an {@link ObjectProperty} does. {@code "duration"} reads how long
 * sailing the route takes. Built via
 * {@link Factories Factories.routeProperty(...)} in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 * @param <R> the type of the property; not checked
 */
@RequiredArgsConstructor
public class RouteProperty<C, T, R> implements BiFunction<C, T, R> {

  private final @NonNull BiFunction<
    ? super C,
    ? super T,
    ? extends Route
  > route;
  private final @NonNull ObjectProperty<Route, R> property;

  /**
   * @throws NullPointerException if the route, or a property part way along the
   *                              path, is {@code null}
   */
  @Override
  public R apply(final C context, final T t) {
    return property.read(route.apply(context, t));
  }
}
