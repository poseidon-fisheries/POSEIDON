/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.market;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;
import uk.ac.ox.poseidon.fishing.vessels.HasAttributes;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * The record of one vessel's completed sale: what sold (as line {@link Item}s)
 * and what didn't.
 */
@Data
public class Sale implements HasAttributes {

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
  /** The selling vessel's attributes when it sold. */
  private final Map<String, Object> attributes;

  /**
   * Records {@code vessel}'s attributes as they are now.
   *
   * @param dateTime when the sale happened
   * @param id       the sale's identifying ID
   * @param market   the market the sale happened at
   * @param vessel   the selling vessel
   * @param items    what sold, one item per (category, species) combination
   * @param unsold   what was offered but didn't sell
   */
  @SuppressFBWarnings(
    value = "EI2",
    justification = "A sale keeps the vessel that sold and the items sold, as given."
  )
  public Sale(
    final LocalDateTime dateTime,
    final String id,
    final Market market,
    final Vessel vessel,
    final List<Item> items,
    final CategorisedCatch unsold
  ) {
    this.dateTime = dateTime;
    this.id = id;
    this.market = market;
    this.vessel = vessel;
    this.items = items;
    this.unsold = unsold;
    this.attributes = ImmutableMap.copyOf(vessel.getAttributes());
  }

  /**
   * @return the total sale value, per currency, summed across {@link #items}
   */
  public Map<CurrencyUnit, Money> summary() {
    return summarise(items);
  }

  /**
   * @return the total value of {@code items}, per currency, each rounded once
   *         to the nearest minor unit of its currency
   */
  public static Map<CurrencyUnit, Money> summarise(
    final Collection<Item> items
  ) {
    // Aggregate as doubles to avoid per-item Money creation; round once per
    // currency.
    final Map<CurrencyUnit, Double> totals = new HashMap<>();
    for (final Item item : items) {
      final CurrencyUnit currencyUnit = item
        .getPrice()
        .getAmount()
        .getCurrencyUnit();
      final double value = item
        .getPrice()
        .valueForKgDouble(item.getContent().asKg());
      totals.merge(currencyUnit, value, Double::sum);
    }
    final Map<CurrencyUnit, Money> result = new HashMap<>();
    totals.forEach((currencyUnit, total) ->
      result.put(
        currencyUnit,
        Money.ofMinor(
          currencyUnit,
          (long) Math.rint(
            total * Math.pow(10, currencyUnit.getDecimalPlaces())
          )
        )
      )
    );
    return result;
  }

  /**
   * One sold line: a (category, species) combination, how much, and at what
   * price.
   */
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
