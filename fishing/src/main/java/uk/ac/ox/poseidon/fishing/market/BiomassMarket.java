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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.poseidon.core.functions.VoidContextConversions.toFunction;

import com.google.common.collect.Maps;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.buckets.BucketBuilder;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.utils.PrefixedId;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A {@link Market} with prices keyed by (catch category, species), with a
 * fallback lookup via {@link Species#covers}, all in the market's
 * {@link #currencyUnit}. A market starts with no prices:
 * they are set through {@link #setPrice}, e.g. by {@link MarketPrice#apply}.
 * Selling a catch splits it into what has a price (sold, broadcast as a
 * {@link Sale}) and what doesn't (left in the sale's {@code unsold} catch).
 */
@Getter
@ToString
public class BiomassMarket implements Market {

  private final Port port;
  private final String code;
  /** The currency every price at this market is in. */
  private final CurrencyUnit currencyUnit;

  // Linked, so that prices are listed in the order they were first set,
  // rather than that of their keys' hash codes
  @Getter(AccessLevel.NONE)
  private final Map<CatchCategory, Map<Species, Price>> prices;

  // Cache resolved prices (including misses) to avoid repeated covers() scans.
  @Getter(AccessLevel.NONE)
  private final Map<CatchCategory, Map<Species, Optional<Price>>> priceCache =
    new HashMap<>();

  // Absent until the first price is set.
  @Getter(AccessLevel.NONE)
  private LocalDateTime lastPriceChangeDateTime;

  private final Function<Vessel, String> saleIds;
  private final EventManager eventManager;

  BiomassMarket(
    final Port port,
    final String code,
    final CurrencyUnit currencyUnit,
    final EventManager eventManager
  ) {
    this.port = port;
    this.code = code;
    this.currencyUnit = checkNotNull(currencyUnit);
    this.prices = new LinkedHashMap<>();
    this.saleIds = toFunction(new PrefixedId(code));
    this.eventManager = eventManager;
  }

  /**
   * Splits {@code categorisedCatch} into sold items (priced via
   * {@link #getPrice}) and unsold remainder (no price for that category, or
   * that species within it), broadcasts the resulting {@link Sale} on
   * {@link #eventManager}, and returns it.
   */
  @Override
  public Sale sell(
    final Vessel vessel,
    final CategorisedCatch categorisedCatch,
    final LocalDateTime dateTime
  ) {
    final List<Sale.Item> soldItems = new ArrayList<>();
    final Map<CatchCategory, BucketBuilder> unsoldByCategory =
      new LinkedHashMap<>();
    split(categorisedCatch, soldItems::add, (catchCategory, bucket) ->
      unsoldByCategory
        .computeIfAbsent(catchCategory, key -> Bucket.newBuilder())
        .add(bucket)
    );
    final CategorisedCatch unsold = unsoldByCategory.isEmpty()
      ? CategorisedCatch.empty()
      : new CategorisedCatch(
          unsoldByCategory
            .entrySet()
            .stream()
            .collect(
              toImmutableMap(Entry::getKey, entry -> entry.getValue().build())
            )
        );
    final Sale sale = new Sale(
      dateTime,
      saleIds.apply(vessel),
      this,
      vessel,
      soldItems,
      unsold
    );
    eventManager.broadcast(sale);
    return sale;
  }

  /**
   * Prices {@code categorisedCatch} via {@link #getPrice}, as {@link #sell}
   * does: what has no price pays nothing. The total is rounded once, to the
   * nearest minor unit of {@link #currencyUnit}, as {@link Sale#summarise}
   * rounds a sale's total.
   */
  @Override
  public Money quote(final CategorisedCatch categorisedCatch) {
    final double[] total = { 0.0 };
    categorisedCatch.getBuckets().forEach((catchCategory, bucket) -> {
      final Map<Species, Price> pricePerSpecies = prices.get(catchCategory);
      if (pricePerSpecies == null) return;
      final Map<Species, Optional<Price>> priceCacheBySpecies =
        priceCache.computeIfAbsent(catchCategory, key -> new HashMap<>());
      bucket.forEachBiomassValue((species, biomass) -> {
        final Optional<Price> price = priceCacheBySpecies.computeIfAbsent(
          species,
          key -> resolvePrice(pricePerSpecies, key)
        );
        if (price.isPresent()) {
          total[0] += price.get().valueForKgDouble(biomass);
        }
      });
    });
    return Money.ofMinor(
      currencyUnit,
      (long) Math.rint(total[0] * Math.pow(10, currencyUnit.getDecimalPlaces()))
    );
  }

  /**
   * Splits {@code categorisedCatch} into priced items (priced via
   * {@link #getPrice}), passed to {@code pricedItemConsumer}, and unpriced
   * catch (no price for that category, or that species within it), passed to
   * {@code unpricedCatchConsumer}. Species with no biomass are skipped, except
   * within a wholly unpriced category.
   */
  private void split(
    final CategorisedCatch categorisedCatch,
    final Consumer<Sale.Item> pricedItemConsumer,
    final BiConsumer<CatchCategory, Bucket> unpricedCatchConsumer
  ) {
    categorisedCatch.getBuckets().forEach((catchCategory, bucket) -> {
      if (prices.get(catchCategory) == null) {
        unpricedCatchConsumer.accept(catchCategory, bucket);
      } else {
        bucket.forEach((species, biomass) -> {
          if (biomass.asKg() <= 0.0) return;
          getPrice(catchCategory, species).ifPresentOrElse(
            price ->
              pricedItemConsumer.accept(
                new Sale.Item(catchCategory, species, biomass, price)
              ),
            () ->
              unpricedCatchConsumer.accept(
                catchCategory,
                Bucket.of(species, biomass)
              )
          );
        });
      }
    });
  }

  /**
   * @return this market's prices, by catch category then species, as a
   *         read-only view
   */
  public Map<CatchCategory, Map<Species, Price>> getPrices() {
    return Collections.unmodifiableMap(
      Maps.transformValues(prices, Collections::unmodifiableMap)
    );
  }

  /**
   * Iterates prices without exposing the mutable backing maps.
   */
  public void forEachPrice(final PriceConsumer consumer) {
    prices.forEach((catchCategory, pricePerSpecies) ->
      pricePerSpecies.forEach((species, price) ->
        consumer.accept(catchCategory, species, price)
      )
    );
  }

  /**
   * @return this market's price for {@code species} under
   *         {@code catchCategory}: a direct match if one is set, otherwise the
   *         first priced species whose {@link Species#covers} the requested
   *         species, otherwise empty
   */
  public Optional<Price> getPrice(
    final CatchCategory catchCategory,
    final Species species
  ) {
    final Map<Species, Price> pricePerSpecies = prices.get(catchCategory);
    if (pricePerSpecies == null) {
      return Optional.empty();
    }
    final Map<Species, Optional<Price>> priceCacheBySpecies =
      priceCache.computeIfAbsent(catchCategory, key -> new HashMap<>());
    return priceCacheBySpecies.computeIfAbsent(species, key ->
      resolvePrice(pricePerSpecies, key)
    );
  }

  @Override
  public Optional<LocalDateTime> getLastPriceChangeDateTime() {
    return Optional.ofNullable(lastPriceChangeDateTime);
  }

  /**
   * Sets (or replaces) this market's price for {@code species} under
   * {@code catchCategory}.
   *
   * @param dateTime when the price changes
   * @throws IllegalArgumentException if {@code price} is not in this market's
   *                                  {@link #currencyUnit}
   */
  public void setPrice(
    final CatchCategory catchCategory,
    final Species species,
    final Price price,
    final LocalDateTime dateTime
  ) {
    checkCurrencyUnit(price);
    prices
      .computeIfAbsent(catchCategory, k -> new LinkedHashMap<>())
      .put(species, price);
    priceCache.remove(catchCategory);
    lastPriceChangeDateTime = dateTime;
  }

  /**
   * @throws IllegalArgumentException if {@code price} is not in this market's
   *                                  {@link #currencyUnit}
   */
  void checkCurrencyUnit(final Price price) {
    final CurrencyUnit priceCurrencyUnit = price.getAmount().getCurrencyUnit();
    checkArgument(
      priceCurrencyUnit.equals(currencyUnit),
      "Market %s trades in %s, not %s.",
      code,
      currencyUnit,
      priceCurrencyUnit
    );
  }

  private Optional<Price> resolvePrice(
    final Map<Species, Price> pricePerSpecies,
    final Species species
  ) {
    // Try direct match first, then fall back to a covers() scan.
    final Price directMatch = pricePerSpecies.get(species);
    if (directMatch != null) {
      return Optional.of(directMatch);
    }
    for (final Entry<Species, Price> entry : pricePerSpecies.entrySet()) {
      if (entry.getKey().covers(species)) {
        return Optional.of(entry.getValue());
      }
    }
    return Optional.empty();
  }

  /**
   * Receives one (catch category, species, price) triple, as passed to
   * {@link #forEachPrice}.
   */
  @FunctionalInterface
  public interface PriceConsumer {
    /**
     * @param catchCategory the category the price applies to
     * @param species       the species the price applies to
     * @param price         the price
     */
    void accept(CatchCategory catchCategory, Species species, Price price);
  }
}
