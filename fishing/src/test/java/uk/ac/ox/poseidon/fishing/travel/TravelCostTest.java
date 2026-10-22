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
    return new Route(destination.x(), Duration.ofHours(destination.y()));
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

    assertThat(travelCost.inContextOf(vessel).valueOf(cell, null)).isEqualTo(
      5.0
    );
  }

  @Test
  void computesTheRouteOncePerValuation() {
    final TravelCost travelCost = new TravelCost(
      routeFunction,
      List.of(distanceCost, durationCost)
    );

    travelCost.inContextOf(vessel).valueOf(cell, null);

    assertThat(routesComputed).hasValue(1);
  }

  @Test
  void throwsWhenCostsAreInDifferentCurrencies() {
    final TravelCost travelCost = new TravelCost(
      routeFunction,
      List.of(distanceCost, chargedTo -> route -> Money.of(USD, 1))
    );

    assertThatThrownBy(() ->
      travelCost.inContextOf(vessel).valueOf(cell, null)
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

    assertThat(travelCost.inContextOf(vessel).valueOf(cell, null)).isEqualTo(
      1.0
    );
    assertThat(
      travelCost.inContextOf(otherVessel).valueOf(cell, null)
    ).isEqualTo(110.0);
  }

  @Test
  void isZeroWithoutCosts() {
    final TravelCost travelCost = new TravelCost(routeFunction, List.of());

    assertThat(travelCost.inContextOf(vessel).valueOf(cell, null)).isEqualTo(
      0.0
    );
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

    final var travelCostForVessel = travelCost.inContextOf(vessel);
    travelCostForVessel.valueOf(cell, null);
    travelCostForVessel.valueOf(new Cell(1, 1), null);

    assertThat(timesGivenTheVessel).hasValue(1);
  }
}
