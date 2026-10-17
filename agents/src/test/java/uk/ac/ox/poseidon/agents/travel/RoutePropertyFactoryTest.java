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
import static uk.ac.ox.poseidon.agents.travel.Factories.routeProperty;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.io.ScenarioLoader;
import uk.ac.ox.poseidon.io.ScenarioWriter;

class RoutePropertyFactoryTest {

  @Test
  void survivesAYamlRoundTripWithOnlyItsPath() {
    final String written = new ScenarioWriter().write(
      Scenario.builder()
        .component("property", routeProperty("duration"))
        .build()
    );
    assertThat(written)
      .contains("propertyPath: duration")
      .doesNotContain("rootClass");
    assertThat(
      new ScenarioLoader().load(written).getComponents().get("property")
    ).isEqualTo(routeProperty("duration"));
  }

  @Test
  void readsTheDurationOfARoute() {
    assertThat(
      routeProperty("duration")
        .get(Scope.GLOBAL_SCOPE)
        .apply(null, new Route(15.0, Duration.ofMinutes(90)))
    ).isEqualTo(Duration.ofMinutes(90));
  }
}
