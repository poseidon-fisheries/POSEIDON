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

package uk.ac.ox.poseidon.fishing.market;

import java.time.LocalDateTime;
import lombok.Value;

/**
 * A price at one market: the price of a species in a catch category there. This
 * is a price assignment, not an event: {@link PriceUpdate} schedules one as a
 * change over time, and {@link Factories#initialPrices} applies a set of them
 * when the simulation is built. A price in a currency its market does not
 * trade in is refused when the assignment is made, not when it is applied,
 * which may be months into a run.
 */
@Value
public class MarketPrice {

  /** The market the price applies to. */
  BiomassMarket market;
  /** The (category, species, price) entry. */
  PriceEntry priceEntry;

  /**
   * @param market     the market the price applies to
   * @param priceEntry the (category, species, price) entry
   * @throws IllegalArgumentException if the entry's price is not in the
   *                                  market's currency
   */
  public MarketPrice(final BiomassMarket market, final PriceEntry priceEntry) {
    market.checkCurrencyUnit(priceEntry.getPrice());
    this.market = market;
    this.priceEntry = priceEntry;
  }

  /**
   * Sets this price on {@link #market}, replacing any price it had for the same
   * entry.
   *
   * @param dateTime when the price changes
   */
  public void apply(final LocalDateTime dateTime) {
    market.setPrice(
      priceEntry.getCatchCategory(),
      priceEntry.getSpecies(),
      priceEntry.getPrice(),
      dateTime
    );
  }
}
