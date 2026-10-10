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

package uk.ac.ox.poseidon.agents.travel;

import org.joda.money.CurrencyMismatchException;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TravelCostTest {

    private static final CurrencyUnit EUR = CurrencyUnit.of("EUR");
    private static final CurrencyUnit USD = CurrencyUnit.of("USD");

    private final Int2D cell = new Int2D(3, 2);
    private final AtomicInteger routesComputed = new AtomicInteger();
    private final Function<Int2D, Route> routeFunction = destination -> {
        routesComputed.incrementAndGet();
        return new Route(destination.x, Duration.ofHours(destination.y));
    };
    private final Function<Route, Money> distanceCost =
        route -> Money.of(EUR, route.getDistanceInKm());
    private final Function<Route, Money> durationCost =
        route -> Money.of(EUR, route.getDuration().toHours());

    @Test
    void sumsItsCostsOverTheRouteToTheCell() {
        final TravelCost travelCost =
            new TravelCost(routeFunction, List.of(distanceCost, durationCost));

        assertThat(travelCost.applyAsDouble(cell, null)).isEqualTo(5.0);
    }

    @Test
    void computesTheRouteOncePerValuation() {
        final TravelCost travelCost =
            new TravelCost(routeFunction, List.of(distanceCost, durationCost));

        travelCost.applyAsDouble(cell, null);

        assertThat(routesComputed).hasValue(1);
    }

    @Test
    void throwsWhenCostsAreInDifferentCurrencies() {
        final TravelCost travelCost = new TravelCost(
            routeFunction,
            List.of(distanceCost, route -> Money.of(USD, 1))
        );

        assertThatThrownBy(() -> travelCost.applyAsDouble(cell, null))
            .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void isZeroWithoutCosts() {
        final TravelCost travelCost = new TravelCost(routeFunction, List.of());

        assertThat(travelCost.applyAsDouble(cell, null)).isEqualTo(0.0);
    }
}
