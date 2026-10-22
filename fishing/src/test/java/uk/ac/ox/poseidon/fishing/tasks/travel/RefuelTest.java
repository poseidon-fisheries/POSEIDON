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

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.FUEL_TANK;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.ORIGIN;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.setTaskObject;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.vesselCruisingAtTenKph;

import java.util.stream.Stream;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.fishing.fuel.FuelStation;
import uk.ac.ox.poseidon.fishing.fuel.FuelStationGrid;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.accounts.Account;
import uk.ac.ox.poseidon.geography.ports.Port;

class RefuelTest {

  private static final CurrencyUnit EUR = CurrencyUnit.of("EUR");

  private static FuelStation station(
    final Port port,
    final EventManager eventManager
  ) {
    return new FuelStation(
      port,
      "F",
      eventManager,
      ignored -> Money.of(EUR, 2.0),
      60.0
    );
  }

  private static Refuel refuelAmong(
    final Vessel vessel,
    final FuelStation... fuelStations
  ) throws Exception {
    final FuelStationGrid fuelStationGrid = mock(FuelStationGrid.class);
    when(fuelStationGrid.getObjectsAt(ORIGIN)).thenAnswer(invocation ->
      Stream.of(fuelStations)
    );
    final Refuel refuel = new Refuel(FUEL_TANK, fuelStationGrid);
    setTaskObject(refuel, vessel);
    return refuel;
  }

  private static Vessel vesselWithAnAccount() {
    final Vessel vessel = vesselCruisingAtTenKph();
    final Trip trip = vessel.getCurrentTrip();
    when(trip.getAccount()).thenReturn(mock(Account.class));
    return vessel;
  }

  @Test
  void awayFromHomeBuysFromTheStationInItsCell() throws Exception {
    final Vessel vessel = vesselWithAnAccount();
    final EventManager eventManager = mock(EventManager.class);

    refuelAmong(vessel, station(mock(Port.class), eventManager)).start();

    verify(eventManager).broadcast(any(FuelStation.Purchase.class));
  }

  @Test
  void atHomeBuysFromTheHomePortStationAmongSeveral() throws Exception {
    final Vessel vessel = vesselWithAnAccount();
    final Port homePort = mock(Port.class);
    when(vessel.isAtHomePort()).thenReturn(true);
    when(vessel.getHomePort()).thenReturn(homePort);
    final EventManager home = mock(EventManager.class);
    final EventManager neighbour = mock(EventManager.class);

    refuelAmong(
      vessel,
      station(mock(Port.class), neighbour),
      station(homePort, home)
    ).start();

    verify(home).broadcast(any(FuelStation.Purchase.class));
    verify(neighbour, never()).broadcast(any());
  }

  @Test
  void awayFromHomeSeveralStationsInACellAreRejected() throws Exception {
    final Refuel refuel = refuelAmong(
      vesselCruisingAtTenKph(),
      station(mock(Port.class), mock(EventManager.class)),
      station(mock(Port.class), mock(EventManager.class))
    );

    assertThatIllegalStateException().isThrownBy(refuel::start);
  }

  @Test
  void aCellWithoutAStationIsRejected() throws Exception {
    final Refuel refuel = refuelAmong(vesselCruisingAtTenKph());

    assertThatIllegalStateException().isThrownBy(refuel::start);
  }
}
