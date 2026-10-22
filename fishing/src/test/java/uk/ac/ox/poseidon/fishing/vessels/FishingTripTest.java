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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingOutcome;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.FishingTripStartEvent;
import uk.ac.ox.poseidon.fishing.trips.TripEvent;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

class FishingTripTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 6, 0);
  private static final Port ORIGIN = mock(Port.class);

  private static Vessel newVessel(final Cell cell, final ModelGrid modelGrid) {
    return newVessel(cell, modelGrid, mock(EventManager.class));
  }

  private static Vessel newVessel(
    final Cell cell,
    final ModelGrid modelGrid,
    final EventManager vesselEventManager
  ) {
    final Vessel vessel = mock(Vessel.class);
    final VesselField vesselField = mock(VesselField.class);
    when(vessel.getVesselField()).thenReturn(vesselField);
    when(vesselField.getModelGrid()).thenReturn(modelGrid);
    when(vessel.getCell()).thenReturn(cell);
    when(vessel.getEventManager()).thenReturn(vesselEventManager);
    return vessel;
  }

  @Test
  void announcesItsStartAndEndAsAFishingTrip() {
    final EventManager vesselEventManager = new SimpleEventManager();
    final Vessel vessel = newVessel(
      new Cell(0, 0),
      mock(ModelGrid.class),
      vesselEventManager
    );
    final FishingTrip trip = new FishingTrip(
      vessel,
      ORIGIN,
      new Cell(1, 1),
      START
    );
    final List<Object> events = new ArrayList<>();
    vesselEventManager.addListener(
      new SimpleListener<>(TripEvent.class, events::add)
    );
    when(vessel.isDocked()).thenReturn(true);
    trip
      .getEventManager()
      .broadcast(new DockingEvent(vessel, ORIGIN, START.plusHours(8)));

    trip.start();
    trip.endTrip(START.plusHours(9), List.of());

    assertThat(events).containsExactly(
      new FishingTripStartEvent(trip),
      new FishingTripEndEvent(trip)
    );
  }

  @Test
  void recordsFishingEventsBroadcastOnItsEventManagerInOrder() {
    final FishingTrip trip = new FishingTrip(
      newVessel(new Cell(0, 0), mock(ModelGrid.class)),
      ORIGIN,
      new Cell(1, 1),
      START
    );
    final FishingEvent first = newFishingEvent();
    final FishingEvent second = newFishingEvent();

    trip.getEventManager().broadcast(first);
    trip.getEventManager().broadcast(second);

    assertThat(trip.getFishingEvents()).containsExactly(first, second);
  }

  @Test
  void recordsAFishingEventBeforeVesselListenersReceiveIt() {
    final EventManager vesselEventManager = new SimpleEventManager();
    final FishingTrip trip = new FishingTrip(
      newVessel(new Cell(0, 0), mock(ModelGrid.class), vesselEventManager),
      ORIGIN,
      new Cell(1, 1),
      START
    );
    final List<List<FishingEvent>> recordedWhenReceived = new ArrayList<>();
    vesselEventManager.addListener(
      new SimpleListener<>(FishingEvent.class, event ->
        recordedWhenReceived.add(List.copyOf(trip.getFishingEvents()))
      )
    );
    final FishingEvent fishingEvent = newFishingEvent();

    trip.getEventManager().broadcast(fishingEvent);

    assertThat(recordedWhenReceived).containsExactly(List.of(fishingEvent));
  }

  @Test
  void fishingEventsCannotBeModifiedFromOutside() {
    final FishingTrip trip = new FishingTrip(
      newVessel(new Cell(0, 0), mock(ModelGrid.class)),
      ORIGIN,
      new Cell(1, 1),
      START
    );

    assertThatThrownBy(() ->
      trip.getFishingEvents().add(newFishingEvent())
    ).isInstanceOf(UnsupportedOperationException.class);
  }

  private static FishingEvent newFishingEvent() {
    return new FishingEvent(
      mock(ExtendedFishingAction.class),
      mock(FishingOutcome.class)
    );
  }
}
