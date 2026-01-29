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

import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.agents.catches.CategorisedCatch;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.species.Species;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_EVEN;

@Data
public class Sale {
    private final LocalDateTime dateTime;
    private final String id;
    private final Market market;
    private final Vessel vessel;
    private final List<Item> items;
    private final CategorisedCatch unsold;

    public Map<CurrencyUnit, Money> summary() {
        final Map<CurrencyUnit, Double> totals = new HashMap<>();
        for (final Item item : items) {
            final CurrencyUnit currency = item.getPrice().getAmount().getCurrencyUnit();
            final double value =
                item.getPrice().valueForKgDouble(item.getContent().asKg());
            totals.merge(currency, value, Double::sum);
        }
        final Map<CurrencyUnit, Money> result = new HashMap<>();
        totals.forEach((currency, total) ->
            result.put(currency, Money.of(currency, total, HALF_EVEN))
        );
        return result;
    }

    @Value
    public static class Item {
        CatchCategory category;
        Species species;
        Content content;
        Price price;

        @Getter(lazy = true)
        Money saleValue = price.valueFor(content);
    }
}
