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

package uk.ac.ox.poseidon.fishing.vessels;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.fishing.tasks.landings.Factories.landCatches;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.fleet;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselComponent;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselSpecification;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.vessels.holds.Hold;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A vessel component a consumer uses but the vessel specification does not
 * list is a wiring error, caught before a simulation of the scenario starts.
 */
class VesselComponentWiringTest {

  private final VesselComponent<Hold> listedHold = vesselComponent(
    object(mock(Hold.class))
  );

  @Test
  void aComponentTheSpecificationLeavesOutFailsBeforeTheSimulationStarts() {
    final VesselComponent<Hold> unlistedHold = vesselComponent(
      object(mock(Hold.class))
    );
    final Scenario scenario = scenarioLanding(unlistedHold);

    assertThatThrownBy(() -> scenario.startNewSimulation(0))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("LandCatchesFactory")
      .hasMessageContaining("not in the vessel specification");
  }

  @Test
  void aComponentTheSpecificationListsPasses() {
    assertThatCode(() ->
      scenarioLanding(listedHold).startNewSimulation(0).finish()
    ).doesNotThrowAnyException();
  }

  private Scenario scenarioLanding(final VesselComponent<Hold> hold) {
    return Scenario.builder()
      .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
      .component(
        "vesselSpecification",
        vesselSpecification(
          fleet(
            object(mock(VesselField.class)),
            object(mock(PortGrid.class)),
            object(mock(MarketGrid.class))
          ),
          behaviour(landCatches(hold, constant(Duration.ZERO)))
        )
          .homePortAttribute("home_port")
          .component("hold", listedHold)
          .build()
      )
      .build();
  }
}
