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

import static uk.ac.ox.poseidon.core.events.Factories.eventManager;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;

import com.google.common.collect.ImmutableList;
import java.util.List;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.fishing.catches.CatchCategoriser;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/** Factories for markets, prices, and the grid that places markets in space. */
@SuppressWarnings("varargs")
public final class Factories {

  private Factories() {}

  /**
   * @param port       the market's port
   * @param marketCode   the market's code; the port's own code is used if this
   *                     is null
   * @param currencyCode the code of the currency the market trades in
   * @return a {@link SimulationScopeFactory} for a {@link BiomassMarket},
   *         without prices
   * @see BiomassMarket
   */
  public static BiomassMarketFactory biomassMarket(
    final Factory<? super SimulationScope, ? extends Port> port,
    final String marketCode,
    final String currencyCode
  ) {
    return new BiomassMarketFactory(
      port,
      marketCode,
      currencyCode,
      eventManager()
    );
  }

  /**
   * @return a {@link SimulationScopeFactory} for a
   *         {@link BiomassSaleAccumulator}
   * @see BiomassSaleAccumulator
   */
  public static BiomassSaleAccumulatorFactory biomassSaleAccumulator() {
    return new BiomassSaleAccumulatorFactory();
  }

  /**
   * @param catchCategoriser sorts the catch before pricing; give it the same
   *                         factory as the vessel's hold, so that both sort the
   *                         catch the same way
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for a
   *         {@link HomePortCatchValuation}
   * @see HomePortCatchValuation
   */
  public static HomePortCatchValuationFactory homePortCatchValuation(
    final Factory<
      ? super VesselScope,
      ? extends CatchCategoriser
    > catchCategoriser
  ) {
    return new HomePortCatchValuationFactory(
      catchCategoriser,
      currentDateTime()
    );
  }

  /**
   * @param marketPrices the market prices to apply, concatenated in order
   * @return a {@link SimulationScopeFactory} for the {@link MarketPrice}s,
   *         applied when the simulation is built
   * @see InitialPricesFactory
   */
  @SafeVarargs
  public static InitialPricesFactory initialPrices(
    final Factory<
      ? super SimulationScope,
      ? extends List<MarketPrice>
    >... marketPrices
  ) {
    return new InitialPricesFactory(
      ImmutableList.copyOf(marketPrices),
      currentDateTime()
    );
  }

  /**
   * @param portGrid locates each market's port
   * @param markets  the markets to place
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
   *         {@link ImmutableMarketGrid}
   * @see MarketGridFactory
   */
  public static <S extends Scope> MarketGridFactory<S> marketGrid(
    final Factory<? super S, ? extends PortGrid> portGrid,
    final Factory<? super S, ? extends List<? extends Market>> markets
  ) {
    return new MarketGridFactory<>(portGrid, markets);
  }

  /**
   * @param market       the market the prices apply to
   * @param priceEntries the (category, species) prices at that market
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for one
   *         {@link MarketPrice} per entry, all at {@code market}
   * @see MarketPricesFactory
   */
  public static <S extends Scope> MarketPricesFactory<S> marketPrices(
    final Factory<? super S, ? extends BiomassMarket> market,
    final Factory<? super S, ? extends List<PriceEntry>> priceEntries
  ) {
    return new MarketPricesFactory<>(market, priceEntries);
  }

  /**
   * @param marketGrid   the markets the prices apply to
   * @param priceEntries the (category, species) prices, the same at every
   *                     market
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for one
   *         {@link MarketPrice} per market and entry
   * @see UniformMarketPricesFactory
   */
  public static <S extends Scope> UniformMarketPricesFactory<S> uniformMarketPrices(
    final Factory<? super S, ? extends MarketGrid> marketGrid,
    final Factory<? super S, ? extends List<PriceEntry>> priceEntries
  ) {
    return new UniformMarketPricesFactory<>(marketGrid, priceEntries);
  }

  /**
   * @param portGrid     the ports to build one market per
   * @param currencyCode the code of the currency every market trades in
   * @return a {@link SimulationScopeFactory} for a list of
   *         {@link BiomassMarket}s, one per port, without prices
   * @see OneBiomassMarketPerPortFactory
   */
  public static OneBiomassMarketPerPortFactory oneBiomassMarketPerPort(
    final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
    final String currencyCode
  ) {
    return new OneBiomassMarketPerPortFactory(
      portGrid,
      currencyCode,
      eventManager()
    );
  }

  /**
   * @param data                  the price table
   * @param dateColumn            column giving each row's effective date
   * @param marketCodeColumn      column giving each row's market code
   * @param speciesCodeColumn     column giving each row's species code
   * @param categoryCodeColumn    column giving each row's catch category code
   * @param priceColumn           column giving each row's price amount
   * @param currencyColumn        column giving each row's currency code
   * @param measurementUnitColumn column giving each row's unit of mass
   * @param marketGrid            the markets whose prices the table updates
   * @param species               the species to match row species codes against
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for the
   *         table's price updates, each dated at its row's date
   * @see PriceUpdatesFromTableFactory
   */
  public static <S extends Scope> PriceUpdatesFromTableFactory<S> priceUpdatesFromTable(
    final Factory<? super S, ? extends Table> data,
    final String dateColumn,
    final String marketCodeColumn,
    final String speciesCodeColumn,
    final String categoryCodeColumn,
    final String priceColumn,
    final String currencyColumn,
    final String measurementUnitColumn,
    final Factory<? super S, ? extends MarketGrid> marketGrid,
    final Factory<? super S, ? extends Iterable<? extends Species>> species
  ) {
    return new PriceUpdatesFromTableFactory<>(
      data,
      dateColumn,
      marketCodeColumn,
      speciesCodeColumn,
      categoryCodeColumn,
      priceColumn,
      currencyColumn,
      measurementUnitColumn,
      marketGrid,
      species
    );
  }

  /**
   * @param catchCategory the entry's catch category
   * @param species       the entry's species
   * @param price         the entry's price
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link PriceEntry}
   * @see PriceEntry
   */
  public static <S extends Scope> PriceEntryFactory<S> priceEntry(
    final Factory<? super S, ? extends CatchCategory> catchCategory,
    final Factory<? super S, ? extends Species> species,
    final Factory<? super S, ? extends Price> price
  ) {
    return new PriceEntryFactory<>(catchCategory, species, price);
  }

  /**
   * @param amount       the amount paid per {@code massUnit}
   * @param currencyCode the code of the currency {@code amount} is in
   * @param massUnit     the unit of mass {@code amount} is quoted per
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link Price}
   * @see Price
   */
  public static PriceFactory price(
    final double amount,
    final String currencyCode,
    final String massUnit
  ) {
    return new PriceFactory(amount, currencyCode, massUnit);
  }
}
