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

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TimeCostTest {

    private static final CurrencyUnit EUR = CurrencyUnit.of("EUR");

    private final Vessel vessel = mock(Vessel.class);
    private final AtomicReference<Money> hourlyCost = new AtomicReference<>(Money.of(EUR, 10));
    private final TimeCost timeCost = new TimeCost(vessel, _ -> hourlyCost.get());
    private final Route route = new Route(15.0, Duration.ofMinutes(90));

    @Test
    void chargesTheHourlyCostForEachHourOfTheRoute() {
        assertThat(timeCost.apply(route)).isEqualTo(Money.of(EUR, 15));
    }

    @Test
    void readsTheVesselsHourlyCostAtEachCall() {
        final Money costBefore = timeCost.apply(route);
        hourlyCost.set(Money.of(EUR, 20));
        final Money costAfter = timeCost.apply(route);

        assertThat(costBefore).isEqualTo(Money.of(EUR, 15));
        assertThat(costAfter).isEqualTo(Money.of(EUR, 30));
    }

    @Test
    void throwsWithoutAnHourlyCost() {
        hourlyCost.set(null);

        assertThatThrownBy(() -> timeCost.apply(route))
            .isInstanceOf(NullPointerException.class);
    }
}
