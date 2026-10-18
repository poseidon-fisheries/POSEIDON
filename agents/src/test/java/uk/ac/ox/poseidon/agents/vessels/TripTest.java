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

package uk.ac.ox.poseidon.agents.vessels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingOutcome;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class TripTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 6, 0);

  private static Vessel newVessel(final Int2D cell, final ModelGrid modelGrid) {
    return newVessel(cell, modelGrid, mock(EventManager.class));
  }

  private static Vessel newVessel(
    final Int2D cell,
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
  void constructorRejectsDestinationOutsideGrid() {
    final ModelGrid modelGrid = mock(ModelGrid.class);
    doThrow(new IllegalArgumentException("outside grid"))
      .when(modelGrid)
      .checkIsInGrid(any(Int2D.class));
    final Vessel vessel = newVessel(new Int2D(0, 0), modelGrid);

    assertThatThrownBy(() ->
      new Trip(vessel, new Int2D(5, 5), START)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void setDestinationRejectsCellOutsideGrid() {
    // ModelGrid mock's checkIsInGrid no-ops by default, so construction
    // succeeds.
    final ModelGrid modelGrid = mock(ModelGrid.class);
    final Vessel vessel = newVessel(new Int2D(0, 0), modelGrid);
    final Trip trip = new Trip(vessel, new Int2D(1, 1), START);

    doThrow(new IllegalArgumentException("outside grid"))
      .when(modelGrid)
      .checkIsInGrid(any(Int2D.class));

    assertThatThrownBy(() -> trip.setDestination(new Int2D(9, 9))).isInstanceOf(
      IllegalArgumentException.class
    );
  }

  @Test
  void setDestinationToTripOriginResetsToOrigin() {
    final ModelGrid modelGrid = mock(ModelGrid.class);
    final Int2D origin = new Int2D(2, 2);
    final Vessel vessel = newVessel(origin, modelGrid);
    final Trip trip = new Trip(vessel, new Int2D(3, 3), START);

    trip.setDestinationToTripOrigin();

    assertThat(trip.getDestination()).isEqualTo(origin);
  }

  @Test
  void recordsFishingEventsBroadcastOnItsEventManagerInOrder() {
    final Trip trip = new Trip(
      newVessel(new Int2D(0, 0), mock(ModelGrid.class)),
      new Int2D(1, 1),
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
    final Trip trip = new Trip(
      newVessel(new Int2D(0, 0), mock(ModelGrid.class), vesselEventManager),
      new Int2D(1, 1),
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
    final Trip trip = new Trip(
      newVessel(new Int2D(0, 0), mock(ModelGrid.class)),
      new Int2D(1, 1),
      START
    );

    assertThatThrownBy(() ->
      trip.getFishingEvents().add(newFishingEvent())
    ).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void recordsWhenItArrivedInPort() {
    final Trip trip = newTrip();

    trip.arriveInPort(START.plusHours(8));

    assertThat(trip.getPortArrivalDateTime()).isEqualTo(START.plusHours(8));
  }

  @Test
  void cannotArriveInPortAwayFromAPort() {
    final Trip trip = newTrip();
    when(trip.getVessel().isAtPort()).thenReturn(false);

    assertThatThrownBy(() -> trip.arriveInPort(START)).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void cannotArriveInPortTwice() {
    final Trip trip = newTrip();
    trip.arriveInPort(START);

    assertThatThrownBy(() -> trip.arriveInPort(START)).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void hasArrivedInPortOnlyOnceItArrives() {
    final Trip trip = newTrip();
    assertThat(trip.hasArrivedInPort()).isFalse();

    trip.arriveInPort(START);

    assertThat(trip.hasArrivedInPort()).isTrue();
  }

  @Test
  void cannotEndBeforeArrivingInPort() {
    final Trip trip = newTrip();

    assertThatThrownBy(() -> trip.endTrip(START)).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void hasNoTimeAtSeaBeforeArrivingInPort() {
    final Trip trip = newTrip();

    assertThatThrownBy(trip::getTotalTimeAtSea).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void timeAtSeaRunsFromTheStartToTheArrivalInPort() {
    final Trip trip = newTrip();

    trip.arriveInPort(START.plusHours(8));

    assertThat(trip.getTotalTimeAtSea()).isEqualTo(Duration.ofHours(8));
  }

  @Test
  void hasNoTotalDurationBeforeItEnds() {
    final Trip trip = newTrip();
    trip.arriveInPort(START.plusHours(8));

    assertThatThrownBy(trip::getTotalDuration).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void totalDurationRunsFromTheStartToTheEnd() {
    final Trip trip = newTrip();
    trip.arriveInPort(START.plusHours(8));

    trip.endTrip(START.plusHours(9));

    assertThat(trip.getTotalDuration()).isEqualTo(Duration.ofHours(9));
  }

  /** A trip starting in port at {@link #START}. */
  private static Trip newTrip() {
    final Vessel vessel = newVessel(new Int2D(0, 0), mock(ModelGrid.class));
    when(vessel.isAtPort()).thenReturn(true);
    return new Trip(vessel, new Int2D(1, 1), START);
  }

  private static FishingEvent newFishingEvent() {
    return new FishingEvent(
      mock(ExtendedFishingAction.class),
      mock(FishingOutcome.class)
    );
  }
}
