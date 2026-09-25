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

/** The record of one vessel's completed sale: what sold (as line {@link Item}s) and what didn't. */
@Data
public class Sale {
    /** When the sale happened. */
    private final LocalDateTime dateTime;
    /** This sale's identifying ID. */
    private final String id;
    /** The market the sale happened at. */
    private final Market market;
    /** The selling vessel. */
    private final Vessel vessel;
    /** What sold, one item per (category, species) combination. */
    private final List<Item> items;
    /** What was offered but didn't sell. */
    private final CategorisedCatch unsold;

    /** @return the total sale value, per currency, summed across {@link #items} */
    public Map<CurrencyUnit, Money> summary() {
        // Aggregate as doubles to avoid per-item Money creation; round once per currency.
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

    /** One sold line: a (category, species) combination, how much, and at what price. */
    @Value
    public static class Item {
        /** The item's catch category. */
        CatchCategory category;
        /** The item's species. */
        Species species;
        /** How much sold. */
        Content content;
        /** The price it sold at. */
        Price price;

        /** The total value of this line, computed lazily on first access. */
        @Getter(lazy = true)
        Money saleValue = price.valueFor(content);
    }
}
