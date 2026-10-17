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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.agents.travel.Factories.routeProperty;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A property of a route read from the thing mapped or, for a value, under the
 * context.
 */
class RoutePropertyFactoryTest {

  private final Route route = new Route(15.0, Duration.ofMinutes(90));

  /** The thing mapped, a route. */
  private final MappingFactory<Scope, Object, Route, Route> theRoute = scope ->
    (context, r) ->
      r;

  @Test
  void readsTheDurationOfTheRouteMapped() {
    assertThat(
      routeProperty(theRoute, "duration")
        .get(Scope.GLOBAL_SCOPE)
        .apply(null, route)
    ).isEqualTo(Duration.ofMinutes(90));
  }

  @Test
  void readsTheDurationOfARouteValue() {
    assertThat(
      routeProperty(constant(route), "duration")
        .get(Scope.GLOBAL_SCOPE)
        .apply(null)
    ).isEqualTo(Duration.ofMinutes(90));
  }

  @Test
  void aWrongPathFailsWhenBuilt() {
    assertThatThrownBy(() ->
      routeProperty(theRoute, "speed").get(Scope.GLOBAL_SCOPE)
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
