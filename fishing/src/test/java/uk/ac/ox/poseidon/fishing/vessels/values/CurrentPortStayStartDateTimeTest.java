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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.vessels.DockingEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.ports.Port;

class CurrentPortStayStartDateTimeTest {

  private static final LocalDateTime CREATION = LocalDateTime.of(
    2026,
    3,
    10,
    0,
    0
  );

  private final Vessel vessel = mock(Vessel.class);
  private final CurrentPortStayStartDateTime currentPortStayStartDateTime =
    new CurrentPortStayStartDateTime(CREATION);
  private final EventManager eventManager = new SimpleEventManager();

  {
    eventManager.addListener(currentPortStayStartDateTime);
    when(vessel.isDocked()).thenReturn(true);
  }

  @Test
  void isTheCreationDateTimeForAVesselThatHasNotDocked() {
    assertThat(currentPortStayStartDateTime.apply(vessel)).isEqualTo(CREATION);
  }

  @Test
  void isTheLastDockingDateTime() {
    dock(CREATION.plusDays(1));
    dock(CREATION.plusDays(3));
    assertThat(currentPortStayStartDateTime.apply(vessel)).isEqualTo(
      CREATION.plusDays(3)
    );
  }

  @Test
  void throwsForAVesselAtSea() {
    when(vessel.isDocked()).thenReturn(false);
    assertThatIllegalStateException().isThrownBy(() ->
      currentPortStayStartDateTime.apply(vessel)
    );
  }

  private void dock(final LocalDateTime dateTime) {
    eventManager.broadcast(
      new DockingEvent(vessel, mock(Port.class), dateTime)
    );
  }
}
