/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.market;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.agents.catches.CategorisedCatch;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_EVEN;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.unit.Units.KILOGRAM;

class SaleTest {

    @Test
    void summaryAggregatesByCurrencyWithDoubleTotals() {
        final CurrencyUnit eur = CurrencyUnit.of("EUR");
        final CurrencyUnit usd = CurrencyUnit.of("USD");
        final Price eurPrice = new Price(Money.of(eur, 2.0), KILOGRAM);
        final Price usdPrice = new Price(Money.of(usd, 1.0), KILOGRAM);
        final CatchCategory category = new CatchCategory("C1");
        final Species species = new Species("S", null, null);

        final List<Sale.Item> items = List.of(
            new Sale.Item(category, species, Biomass.ofKg(10.0), eurPrice),
            new Sale.Item(category, species, Biomass.ofKg(5.0), eurPrice),
            new Sale.Item(category, species, Biomass.ofKg(3.0), usdPrice)
        );

        final Sale sale = new Sale(
            LocalDateTime.now(),
            "sale-1",
            null,
            null,
            items,
            CategorisedCatch.empty()
        );

        final Map<CurrencyUnit, Money> summary = sale.summary();

        assertThat(summary).containsEntry(eur, Money.of(eur, 30.0, HALF_EVEN));
        assertThat(summary).containsEntry(usd, Money.of(usd, 3.0, HALF_EVEN));
    }
}
