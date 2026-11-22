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

package uk.ac.ox.poseidon.fishing.vessels.values;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.vesselProperty;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.io.ScenarioLoader;
import uk.ac.ox.poseidon.io.ScenarioWriter;

class VesselPropertyFactoryTest {

  @Test
  void survivesAYamlRoundTripWithOnlyItsPath() {
    final String written = new ScenarioWriter().write(
      Scenario.builder().component("property", vesselProperty("id")).build()
    );
    assertThat(written)
      .contains("propertyPath: id")
      .doesNotContain("rootClass");
    assertThat(
      new ScenarioLoader().load(written).getComponents().get("property")
    ).isEqualTo(vesselProperty("id"));
  }

  @Test
  void readsThePropertyOfTheVessel() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getId()).thenReturn("V1");

    assertThat(
      vesselProperty("id").get(Scope.GLOBAL_SCOPE).apply(vessel)
    ).isEqualTo("V1");
  }
}
