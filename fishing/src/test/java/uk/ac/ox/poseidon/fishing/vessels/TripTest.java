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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
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
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

class TripTest {

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
  void constructorRejectsDestinationOutsideGrid() {
    final ModelGrid modelGrid = mock(ModelGrid.class);
    doThrow(new IllegalArgumentException("outside grid"))
      .when(modelGrid)
      .checkIsInGrid(any(Cell.class));
    final Vessel vessel = newVessel(new Cell(0, 0), modelGrid);

    assertThatThrownBy(() ->
      new Trip(vessel, ORIGIN, new Cell(5, 5), START)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void setDestinationRejectsCellOutsideGrid() {
    // ModelGrid mock's checkIsInGrid no-ops by default, so construction
    // succeeds.
    final ModelGrid modelGrid = mock(ModelGrid.class);
    final Vessel vessel = newVessel(new Cell(0, 0), modelGrid);
    final Trip trip = new Trip(vessel, ORIGIN, new Cell(1, 1), START);

    doThrow(new IllegalArgumentException("outside grid"))
      .when(modelGrid)
      .checkIsInGrid(any(Cell.class));

    assertThatThrownBy(() -> trip.setDestination(new Cell(9, 9))).isInstanceOf(
      IllegalArgumentException.class
    );
  }

  @Test
  void setDestinationToTripOriginHeadsForTheOriginPort() {
    final Trip trip = newTrip();

    trip.setDestinationToTripOrigin();

    assertThat(trip.getDestination()).isEqualTo(ORIGIN);
  }

  @Test
  void recordsFishingEventsBroadcastOnItsEventManagerInOrder() {
    final Trip trip = new Trip(
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
    final Trip trip = new Trip(
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
    final Trip trip = new Trip(
      newVessel(new Cell(0, 0), mock(ModelGrid.class)),
      ORIGIN,
      new Cell(1, 1),
      START
    );

    assertThatThrownBy(() ->
      trip.getFishingEvents().add(newFishingEvent())
    ).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void recordsWhenTheVesselDockedAsItsArrivalInPort() {
    final Trip trip = newTrip();

    dock(trip, START.plusHours(8));

    assertThat(trip.getPortArrivalDateTime()).isEqualTo(START.plusHours(8));
  }

  @Test
  void hasArrivedInPortOnlyWhileTheVesselIsDocked() {
    final Trip trip = newTrip();
    assertThat(trip.hasArrivedInPort()).isFalse();

    dock(trip, START.plusHours(8));
    assertThat(trip.hasArrivedInPort()).isTrue();

    trip
      .getEventManager()
      .broadcast(
        new UndockingEvent(trip.getVessel(), ORIGIN, START.plusHours(9))
      );
    assertThat(trip.hasArrivedInPort()).isFalse();
  }

  @Test
  void cannotEndAtSea() {
    final Trip trip = newTrip();

    assertThatThrownBy(() -> trip.endTrip(START))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cannot end at sea");
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

    dock(trip, START.plusHours(8));

    assertThat(trip.getTotalTimeAtSea()).isEqualTo(Duration.ofHours(8));
  }

  @Test
  void hasNoTotalDurationBeforeItEnds() {
    final Trip trip = newTrip();
    dock(trip, START.plusHours(8));

    assertThatThrownBy(trip::getTotalDuration).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void totalDurationRunsFromTheStartToTheEnd() {
    final Trip trip = newTrip();
    dock(trip, START.plusHours(8));

    trip.endTrip(START.plusHours(9));

    assertThat(trip.getTotalDuration()).isEqualTo(Duration.ofHours(9));
  }

  /** A trip starting from {@link #ORIGIN} at {@link #START}. */
  private static Trip newTrip() {
    final Vessel vessel = newVessel(new Cell(0, 0), mock(ModelGrid.class));
    return new Trip(vessel, ORIGIN, new Cell(1, 1), START);
  }

  /**
   * Broadcasts {@code trip}'s vessel docking at {@link #ORIGIN}, as the vessel
   * does, and has the vessel report itself docked.
   */
  private static void dock(final Trip trip, final LocalDateTime dateTime) {
    when(trip.getVessel().isDocked()).thenReturn(true);
    trip
      .getEventManager()
      .broadcast(new DockingEvent(trip.getVessel(), ORIGIN, dateTime));
  }

  private static FishingEvent newFishingEvent() {
    return new FishingEvent(
      mock(ExtendedFishingAction.class),
      mock(FishingOutcome.class)
    );
  }
}
