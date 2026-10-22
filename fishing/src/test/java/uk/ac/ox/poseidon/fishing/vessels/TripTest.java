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
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.trips.TripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.TripEvent;
import uk.ac.ox.poseidon.fishing.trips.TripStartEvent;
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
  void announcesItsStartAndEndAsATripOfNoParticularKind() {
    final EventManager vesselEventManager = new SimpleEventManager();
    final Trip trip = new Trip(
      newVessel(new Cell(0, 0), mock(ModelGrid.class), vesselEventManager),
      ORIGIN,
      new Cell(1, 1),
      START
    );
    final List<Object> events = new ArrayList<>();
    vesselEventManager.addListener(
      new SimpleListener<>(TripEvent.class, events::add)
    );
    dock(trip, START.plusHours(8));

    trip.start();
    trip.endTrip(START.plusHours(9), List.of());

    assertThat(events).containsExactly(
      new TripStartEvent(trip),
      new TripEndEvent(trip)
    );
  }

  @Test
  void endingMakesTheOpenDockingItsArrivalInPort() {
    final Trip trip = newTrip();
    dock(trip, START.plusHours(8));

    trip.endTrip(START.plusHours(9), List.of());

    assertThat(trip.getPortArrivalDateTime()).isEqualTo(START.plusHours(8));
    assertThat(trip.getPortCalls()).isEmpty();
  }

  @Test
  void anUndockingMakesTheOpenDockingAPortCall() {
    final Trip trip = newTrip();
    dock(trip, START.plusHours(3));

    undock(trip, START.plusHours(4));

    assertThat(trip.getPortCalls()).containsExactly(
      new PortCall(ORIGIN, START.plusHours(3), START.plusHours(4))
    );
  }

  @Test
  void cannotEndAtSea() {
    final Trip trip = newTrip();

    assertThatThrownBy(() -> trip.endTrip(START, List.of()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cannot end at sea");
  }

  @Test
  void hasNoTotalTimeAtSeaBeforeItEnds() {
    final Trip trip = newTrip();
    dock(trip, START.plusHours(8));

    assertThatThrownBy(trip::getTotalTimeAtSea)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("has not ended");
  }

  @Test
  void totalTimeAtSeaRunsFromTheStartToTheArrivalLessItsPortCalls() {
    final Trip trip = newTrip();
    dock(trip, START.plusHours(3));
    undock(trip, START.plusHours(4));
    dock(trip, START.plusHours(8));

    trip.endTrip(START.plusHours(9), List.of());

    assertThat(trip.getTotalTimeAtSea()).isEqualTo(Duration.ofHours(7));
  }

  @Test
  void timeAtSeaSoFarRunsToTheDateAtSea() {
    final Trip trip = newTrip();

    assertThat(trip.getTimeAtSeaUntil(START.plusHours(5))).isEqualTo(
      Duration.ofHours(5)
    );
  }

  @Test
  void timeAtSeaSoFarLeavesOutPortCallsAndAStopInProgress() {
    final Trip trip = newTrip();
    dock(trip, START.plusHours(3));
    undock(trip, START.plusHours(4));
    dock(trip, START.plusHours(6));

    assertThat(trip.getTimeAtSeaUntil(START.plusHours(7))).isEqualTo(
      Duration.ofHours(5)
    );
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

    trip.endTrip(START.plusHours(9), List.of());

    assertThat(trip.getTotalDuration()).isEqualTo(Duration.ofHours(9));
  }

  @Test
  void chargesItsCostOnFinalTotalsBeforeBroadcastingItsEnd() {
    final EventManager vesselEventManager = new SimpleEventManager();
    final Trip trip = new Trip(
      newVessel(new Cell(0, 0), mock(ModelGrid.class), vesselEventManager),
      ORIGIN,
      new Cell(1, 1),
      START
    );
    final List<Money> balancesAtEnd = new ArrayList<>();
    vesselEventManager.addListener(
      new SimpleListener<>(TripEndEvent.class, event ->
        balancesAtEnd.add(
          event.getTrip().getAccount().getBalances().get(CurrencyUnit.EUR)
        )
      )
    );
    dock(trip, START.plusHours(8));

    trip.endTrip(
      START.plusHours(9),
      List.of(finished ->
        Money.of(CurrencyUnit.EUR, finished.getTotalTimeAtSea().toHours())
      )
    );

    assertThat(balancesAtEnd).containsExactly(Money.of(CurrencyUnit.EUR, -8));
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

  /**
   * Broadcasts {@code trip}'s vessel undocking from {@link #ORIGIN}, as the
   * vessel does, and has the vessel report itself at sea.
   */
  private static void undock(final Trip trip, final LocalDateTime dateTime) {
    when(trip.getVessel().isDocked()).thenReturn(false);
    trip
      .getEventManager()
      .broadcast(new UndockingEvent(trip.getVessel(), ORIGIN, dateTime));
  }
}
