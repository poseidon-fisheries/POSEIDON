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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.*;

import com.badlogic.gdx.ai.btree.Task.Status;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

class TravelDirectlyTest {

  @Test
  void takesTheDistanceOverTheCruisingSpeed() throws Exception {
    final DistanceCalculator distanceCalculator = mock(
      DistanceCalculator.class
    );
    when(distanceCalculator.distanceInKm(ORIGIN, DESTINATION)).thenReturn(5.0);
    final Vessel vessel = vesselCruisingAtTenKph();
    final TravelDirectly travelDirectly = new TravelDirectly(
      ENGINE,
      FUEL_TANK,
      distanceCalculator,
      v -> NOW
    );
    setTaskObject(travelDirectly, vessel);

    travelDirectly.start();
    travelDirectly.execute();

    verify(vessel).setTaskDuration(Duration.ofMinutes(30));
  }

  @Test
  void takesTheFuelTheEngineBurnsFromTheTank() throws Exception {
    final DistanceCalculator distanceCalculator = mock(
      DistanceCalculator.class
    );
    when(distanceCalculator.distanceInKm(ORIGIN, DESTINATION)).thenReturn(5.0);
    final Vessel vessel = vesselCruisingAtTenKph();
    final TravelDirectly travelDirectly = new TravelDirectly(
      ENGINE,
      FUEL_TANK,
      distanceCalculator,
      v -> NOW
    );
    setTaskObject(travelDirectly, vessel);

    travelDirectly.start();
    travelDirectly.complete();

    verify(FUEL_TANK.of(vessel)).consumeFuel(15.0);
  }

  @Test
  void movesTheVesselToTheDestinationAndBroadcastsTheTravel() throws Exception {
    final DistanceCalculator distanceCalculator = mock(
      DistanceCalculator.class
    );
    when(distanceCalculator.distanceInKm(ORIGIN, DESTINATION)).thenReturn(5.0);
    final Vessel vessel = vesselCruisingAtTenKph();
    final TravelDirectly travelDirectly = new TravelDirectly(
      ENGINE,
      FUEL_TANK,
      distanceCalculator,
      v -> NOW
    );
    setTaskObject(travelDirectly, vessel);

    travelDirectly.start();
    final Status status = travelDirectly.complete();

    assertThat(status).isEqualTo(Status.SUCCEEDED);
    verify(vessel).setCurrentCell(DESTINATION);
    final ArgumentCaptor<TravelEvent> event = ArgumentCaptor.forClass(
      TravelEvent.class
    );
    verify(vessel.getCurrentTrip().getEventManager()).broadcast(
      event.capture()
    );
    assertThat(event.getValue().getOrigin()).isEqualTo(ORIGIN);
    assertThat(event.getValue().getDestination()).isEqualTo(DESTINATION);
    assertThat(event.getValue().getStartDateTime()).isEqualTo(NOW);
    assertThat(event.getValue().getEndDateTime()).isEqualTo(NOW);
  }
}
