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

package uk.ac.ox.poseidon.fishing.fuel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.fishing.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.geography.ports.Port;

class FuelStationTest {

  private static final CurrencyUnit EUR = CurrencyUnit.of("EUR");

  private final AtomicReference<Money> price = new AtomicReference<>(
    Money.of(EUR, 2.0)
  );

  private FuelStation newStation(final EventManager eventManager) {
    return new FuelStation(
      mock(Port.class),
      "F1",
      eventManager,
      ignored -> price.get(),
      60.0
    );
  }

  @Test
  void constructorRejectsNonPositivePumpRate() {
    assertThatThrownBy(() ->
      new FuelStation(
        mock(Port.class),
        "F1",
        mock(EventManager.class),
        ignored -> Money.of(EUR, 2.0),
        0.0
      )
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void negativePriceIsRejectedWhenRead() {
    final FuelStation station = newStation(mock(EventManager.class));
    price.set(Money.of(EUR, -1.0));
    assertThatIllegalArgumentException().isThrownBy(() ->
      station.priceFor(1.0)
    );
  }

  @Test
  void priceIsReadOnEachSale() {
    final FuelStation station = newStation(mock(EventManager.class));
    assertThat(station.priceFor(10.0)).isEqualTo(Money.of(EUR, 20.0));
    price.set(Money.of(EUR, 3.0));
    assertThat(station.priceFor(10.0)).isEqualTo(Money.of(EUR, 30.0));
  }

  @Test
  void refillBroadcastsPurchaseAndFillsTank() {
    final EventManager eventManager = mock(EventManager.class);
    final FuelStation station = newStation(eventManager);

    final FuelTank fuelTank = mock(FuelTank.class);
    when(fuelTank.getCapacityInLitres()).thenReturn(100.0);
    when(fuelTank.getCurrentFuelInLitres()).thenReturn(40.0);

    final FuelStation.Purchase purchase = station.refill(fuelTank);

    verify(fuelTank).addFuel(eq(60.0));
    assertThat(purchase.getLitres()).isEqualTo(60.0);
    assertThat(purchase.getPrice()).isEqualTo(station.priceFor(60.0));
    verify(eventManager).broadcast(purchase);
  }

  @Test
  void refillGivesATankOverItsCapacityNothing() {
    final FuelStation station = newStation(mock(EventManager.class));

    final FuelTank fuelTank = mock(FuelTank.class);
    when(fuelTank.getCapacityInLitres()).thenReturn(100.0);
    when(fuelTank.getCurrentFuelInLitres()).thenReturn(120.0);

    final FuelStation.Purchase purchase = station.refill(fuelTank);

    verify(fuelTank).addFuel(eq(0.0));
    assertThat(purchase.getLitres()).isZero();
  }
}
