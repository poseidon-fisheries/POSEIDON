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

package uk.ac.ox.poseidon.fishing.travel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.joda.money.CurrencyMismatchException;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.grids.Cell;

class TravelCostTest {

  private static final CurrencyUnit EUR = CurrencyUnit.of("EUR");
  private static final CurrencyUnit USD = CurrencyUnit.of("USD");

  private final Vessel vessel = mock(Vessel.class);
  private final Cell cell = new Cell(3, 2);
  private final AtomicInteger routesComputed = new AtomicInteger();
  private final BiFunction<Vessel, Cell, Route> routeFunction = (
    sailedBy,
    destination
  ) -> {
    routesComputed.incrementAndGet();
    return new Route(destination.getX(), Duration.ofHours(destination.getY()));
  };
  private final Function<Vessel, Function<Route, Money>> distanceCost =
    chargedTo -> route -> Money.of(EUR, route.getDistanceInKm());
  private final Function<Vessel, Function<Route, Money>> durationCost =
    chargedTo -> route -> Money.of(EUR, route.getDuration().toHours());

  @Test
  void sumsItsCostsOverTheRouteToTheCell() {
    final TravelCost travelCost = new TravelCost(
      routeFunction,
      List.of(distanceCost, durationCost)
    );

    assertThat(travelCost.forVessel(vessel).applyAsDouble(cell)).isEqualTo(5.0);
  }

  @Test
  void computesTheRouteOncePerValuation() {
    final TravelCost travelCost = new TravelCost(
      routeFunction,
      List.of(distanceCost, durationCost)
    );

    travelCost.forVessel(vessel).applyAsDouble(cell);

    assertThat(routesComputed).hasValue(1);
  }

  @Test
  void throwsWhenCostsAreInDifferentCurrencies() {
    final TravelCost travelCost = new TravelCost(
      routeFunction,
      List.of(distanceCost, chargedTo -> route -> Money.of(USD, 1))
    );

    assertThatThrownBy(() ->
      travelCost.forVessel(vessel).applyAsDouble(cell)
    ).isInstanceOf(CurrencyMismatchException.class);
  }

  @Test
  void givesTheVesselToTheRouteAndToEachCost() {
    final Vessel otherVessel = mock(Vessel.class);
    final TravelCost travelCost = new TravelCost(
      (sailedBy, destination) ->
        new Route(sailedBy == vessel ? 1 : 10, Duration.ZERO),
      List.of(
        distanceCost,
        chargedTo -> route -> Money.of(EUR, chargedTo == vessel ? 0 : 100)
      )
    );

    assertThat(travelCost.forVessel(vessel).applyAsDouble(cell)).isEqualTo(1.0);
    assertThat(travelCost.forVessel(otherVessel).applyAsDouble(cell)).isEqualTo(
      110.0
    );
  }

  @Test
  void isZeroWithoutCosts() {
    final TravelCost travelCost = new TravelCost(routeFunction, List.of());

    assertThat(travelCost.forVessel(vessel).applyAsDouble(cell)).isEqualTo(0.0);
  }

  @Test
  void givesTheVesselToEachCostOnceForAllTheCellsValuedThen() {
    final AtomicInteger timesGivenTheVessel = new AtomicInteger();
    final TravelCost travelCost = new TravelCost(
      routeFunction,
      List.of(chargedTo -> {
        timesGivenTheVessel.incrementAndGet();
        return route -> Money.of(EUR, 1);
      })
    );

    final var travelCostForVessel = travelCost.forVessel(vessel);
    travelCostForVessel.applyAsDouble(cell);
    travelCostForVessel.applyAsDouble(new Cell(1, 1));

    assertThat(timesGivenTheVessel).hasValue(1);
  }

  @Test
  void snapshotsTheHourlyRateAndReadsItAfreshForTheNextVisit() {
    final AtomicReference<Money> hourlyCost = new AtomicReference<>(
      Money.of(EUR, 2)
    );
    final TravelCost cost = new TravelCost(
      routeFunction,
      List.of(new TimeCost(receiver -> hourlyCost.get()))
    );
    final var firstVisit = cost.forVessel(vessel);
    assertThat(firstVisit.applyAsDouble(cell)).isEqualTo(4.0);
    hourlyCost.set(Money.of(EUR, 3));
    assertThat(firstVisit.applyAsDouble(new Cell(1, 1))).isEqualTo(2.0);
    assertThat(cost.forVessel(vessel).applyAsDouble(cell)).isEqualTo(6.0);
  }
}
