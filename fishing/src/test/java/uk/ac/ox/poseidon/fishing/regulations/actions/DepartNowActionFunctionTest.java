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

package uk.ac.ox.poseidon.fishing.regulations.actions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.giveComponent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.FactoryComponent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.ports.Port;

final class DepartNowActionFunctionTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 7, 0);

  @Test
  void buildsADepartureOfTheVesselNowWithItsCurrentGear() {
    final Vessel vessel = mock(Vessel.class, RETURNS_DEEP_STUBS);
    when(vessel.getAttributes()).thenReturn(Map.of());
    when(vessel.getHomePort()).thenReturn(Optional.of(mock(Port.class)));
    final Gear gear = mock(Gear.class);
    final VesselComponent<Gear> gearComponent = new FactoryComponent<>(
      scope -> gear
    );
    giveComponent(vessel, gearComponent, gear);

    final DepartNow action = new DepartNowActionFunction(
      gearComponent,
      () -> NOW
    ).apply(vessel);

    assertThat(action.getAgent()).isSameAs(vessel);
    assertThat(action.getStartDateTime()).isEqualTo(NOW);
    assertThat(action.getGear()).isSameAs(gear);
  }
}
