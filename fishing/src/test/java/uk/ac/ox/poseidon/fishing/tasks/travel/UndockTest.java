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

package uk.ac.ox.poseidon.fishing.tasks.travel;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.NOW;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.setTaskObject;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.vesselCruisingAtTenKph;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class UndockTest {

  @Test
  void undocksTheVesselDuringATrip() throws Exception {
    final Vessel vessel = vesselCruisingAtTenKph();
    final Undock undock = new Undock(_ -> NOW);
    setTaskObject(undock, vessel);

    undock.execute();

    verify(vessel).undock(NOW);
  }

  @Test
  void throwsOutsideATrip() throws Exception {
    final Vessel vessel = vesselCruisingAtTenKph();
    when(vessel.getCurrentTrip()).thenReturn(null);
    final Undock undock = new Undock(_ -> NOW);
    setTaskObject(undock, vessel);

    assertThatThrownBy(undock::execute)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cannot undock outside a trip");
    verify(vessel, never()).undock(any());
  }
}
