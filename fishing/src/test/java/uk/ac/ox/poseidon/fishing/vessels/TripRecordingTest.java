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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.fishing.choices.Factories.destinationCell;
import static uk.ac.ox.poseidon.fishing.choices.Factories.profitPerHour;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.HasDateTime;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.TripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.TripEvent;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

class TripRecordingTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 6, 0);
  private static final Cell CELL = new Cell(1, 1);
  private final Vessel vessel = mock(Vessel.class);
  private final ModelGrid grid = mock(ModelGrid.class);
  private final Port port = mock(Port.class);

  private FishingTrip trip(final boolean cellDestination) {
    final VesselField field = mock(VesselField.class);
    when(vessel.getVesselField()).thenReturn(field);
    when(field.getModelGrid()).thenReturn(grid);
    when(vessel.getEventManager()).thenReturn(new SimpleEventManager());
    when(vessel.isDocked()).thenReturn(true);
    return new FishingTrip(vessel, port, cellDestination ? CELL : port, START);
  }

  private void dock(final Trip trip, final LocalDateTime time) {
    trip.getEventManager().broadcast(new DockingEvent(vessel, port, time));
  }

  @Test
  void destinationsIncludeInitialAndRepeatedAssignmentsAndCannotBeChangedByReaders() {
    final FishingTrip trip = trip(true);
    trip.setDestinationToTripOrigin();
    trip.setDestination(CELL);
    assertThat(trip.getDestinations()).containsExactly(CELL, port, CELL);
    assertThatThrownBy(() -> trip.getDestinations().clear()).isInstanceOf(
      UnsupportedOperationException.class
    );
    assertThat(
      destinationCell().get(GLOBAL_SCOPE).apply(new FishingTripEndEvent(trip))
    ).contains(CELL);
  }

  @Test
  void aRejectedDestinationIsNotRecorded() {
    final FishingTrip trip = trip(true);
    final Cell outside = new Cell(99, 99);
    doThrow(new IllegalArgumentException("outside"))
      .when(grid)
      .checkIsInGrid(outside);
    assertThatThrownBy(() -> trip.setDestination(outside)).isInstanceOf(
      IllegalArgumentException.class
    );
    assertThat(trip.getDestinations()).containsExactly(CELL);
    assertThat(trip.getDestination()).isEqualTo(CELL);
  }

  @Test
  void portsGiveNoKeyAndSeveralDistinctCellsFail() {
    final FishingTrip trip = trip(false);
    assertThat(
      destinationCell().get(GLOBAL_SCOPE).apply(new FishingTripEndEvent(trip))
    ).isEmpty();
    trip.setDestination(CELL);
    trip.setDestination(new Cell(2, 2));
    assertThatIllegalStateException().isThrownBy(() ->
      destinationCell().get(GLOBAL_SCOPE).apply(new FishingTripEndEvent(trip))
    );
  }

  @Test
  void tripEventsGiveTheirVesselAndTheFishingEndGivesItsEndRatherThanArrivalTime() {
    final FishingTrip trip = trip(true);
    final List<TripEvent> events = new ArrayList<>();
    trip
      .getEventManager()
      .addListener(new SimpleListener<>(TripEvent.class, events::add));
    trip.start();
    dock(trip, START.plusHours(2));
    trip.endTrip(START.plusHours(3), List.of());
    assertThat(trip).isInstanceOf(HasVessel.class);
    assertThat(events)
      .hasSize(2)
      .allSatisfy(event -> assertThat(event.getVessel()).isSameAs(vessel));
    assertThat(((HasDateTime) events.getLast()).getDateTime()).isEqualTo(
      START.plusHours(3)
    );
    assertThat(new DockingEvent(vessel, port, START).getVessel()).isSameAs(
      vessel
    );
  }

  @Test
  void profitUsesTheFinalAccountAndTimeAtSea() {
    final FishingTrip trip = trip(true);
    trip.getAccount().add(Money.of(CurrencyUnit.EUR, 120));
    final List<Double> profits = new ArrayList<>();
    trip
      .getEventManager()
      .addListener(
        new SimpleListener<>(FishingTripEndEvent.class, event ->
          profits.add(profitPerHour().get(GLOBAL_SCOPE).apply(event))
        )
      );
    dock(trip, START.plusMinutes(30));
    trip
      .getEventManager()
      .broadcast(new UndockingEvent(vessel, port, START.plusMinutes(45)));
    dock(trip, START.plusHours(2).plusMinutes(15));
    trip.endTrip(
      START.plusHours(3),
      List.of(_ -> Money.of(CurrencyUnit.EUR, 20))
    );
    assertThat(profits).containsExactly(50.0);

    final Trip shortTrip = mock(Trip.class);
    when(shortTrip.getTotalTimeAtSea()).thenReturn(Duration.ofSeconds(36));
    when(shortTrip.getAccount()).thenReturn(trip.getAccount());
    assertThat(
      profitPerHour().get(GLOBAL_SCOPE).apply(new TripEndEvent(shortTrip))
    ).isEqualTo(10_000.0);
  }

  @Test
  void emptyAccountGivesZeroButNoTimeAtSeaFailsEvenWithAnEmptyAccount() {
    final FishingTrip trip = trip(true);
    dock(trip, START.plusHours(2));
    trip.endTrip(START.plusHours(3), List.of());
    assertThat(
      profitPerHour().get(GLOBAL_SCOPE).apply(new TripEndEvent(trip))
    ).isZero();
    final FishingTrip noTime = trip(true);
    dock(noTime, START);
    noTime.endTrip(START, List.of());
    assertThatIllegalStateException().isThrownBy(() ->
      profitPerHour().get(GLOBAL_SCOPE).apply(new TripEndEvent(noTime))
    );
  }

  @Test
  void multipleCurrenciesFail() {
    final FishingTrip trip = trip(true);
    trip.getAccount().add(Money.of(CurrencyUnit.EUR, 10));
    trip.getAccount().add(Money.of(CurrencyUnit.USD, 20));
    dock(trip, START.plusHours(1));
    trip.endTrip(START.plusHours(2), List.of());
    assertThatIllegalStateException().isThrownBy(() ->
      profitPerHour().get(GLOBAL_SCOPE).apply(new TripEndEvent(trip))
    );
  }
}
