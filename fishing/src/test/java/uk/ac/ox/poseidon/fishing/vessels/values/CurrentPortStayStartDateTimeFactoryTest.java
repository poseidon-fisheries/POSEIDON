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
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentScopeOf;

import java.time.LocalDateTime;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.vessels.DockingEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.geography.ports.Port;

class CurrentPortStayStartDateTimeFactoryTest {

  private static final LocalDateTime BUILD = LocalDateTime.of(
    2026,
    3,
    10,
    0,
    0
  );

  private final Simulation simulation = mock(Simulation.class);
  private final Vessel vessel = mock(Vessel.class);
  private final EventManager vesselEventManager = new SimpleEventManager();
  private LocalDateTime now = BUILD;
  private final CurrentPortStayStartDateTimeFactory factory =
    new CurrentPortStayStartDateTimeFactory(scope -> _ -> now);
  private Function<Vessel, LocalDateTime> currentPortStayStartDateTime;

  @BeforeEach
  void buildForTheVessel() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(vessel.getProductStore()).thenReturn(new ProductStore());
    when(vessel.getEventManager()).thenReturn(vesselEventManager);
    when(vessel.isDocked()).thenReturn(true);
    currentPortStayStartDateTime = factory.get(componentScope());
  }

  private VesselComponentScope componentScope() {
    return componentScopeOf(
      new VesselScope(new SimulationScope(simulation), vessel)
    );
  }

  @Test
  void startsTheFirstStayWhenItIsBuilt() {
    now = BUILD.plusHours(5);
    assertThat(currentPortStayStartDateTime.apply(vessel)).isEqualTo(BUILD);
  }

  @Test
  void hearsTheDockingsOnTheVesselsEventManager() {
    vesselEventManager.broadcast(
      new DockingEvent(vessel, mock(Port.class), BUILD.plusDays(2))
    );
    assertThat(currentPortStayStartDateTime.apply(vessel)).isEqualTo(
      BUILD.plusDays(2)
    );
  }

  @Test
  void aComponentBuiltLaterReadsTheSameRecord() {
    now = BUILD.plusDays(1);
    assertThat(factory.get(componentScope()).apply(vessel)).isEqualTo(BUILD);
  }
}
