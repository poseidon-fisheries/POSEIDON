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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.agents.catches.CategorisedCatch;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.buckets.BucketBuilder;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.utils.PrefixedIdSupplier;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;

@Getter
@ToString
public class BiomassMarket implements Market {

    private final Port port;
    private final String code;
    private final Map<CatchCategory, Map<Species, Price>> prices;
    // Cache resolved prices (including misses) to avoid repeated covers() scans.
    @Getter(AccessLevel.NONE)
    private final Map<CatchCategory, Map<Species, Optional<Price>>> priceCache = new HashMap<>();
    private final Supplier<String> saleIdSupplier;
    private final EventManager eventManager;

    BiomassMarket(
        final Port port,
        final String code,
        final Map<CatchCategory, Map<Species, Price>> prices,
        final EventManager eventManager
    ) {
        this.port = port;
        this.code = code;
        this.prices = prices
            .entrySet()
            .stream()
            .collect(toMap(
                Entry::getKey,
                entry -> new HashMap<>(entry.getValue())
            ));
        this.saleIdSupplier = new PrefixedIdSupplier(code);
        this.eventManager = eventManager;
    }

    @Override
    public Sale sell(
        final Vessel vessel,
        final CategorisedCatch categorisedCatch,
        final LocalDateTime dateTime
    ) {
        final List<Sale.Item> soldItems = new ArrayList<>();
        final Map<CatchCategory, BucketBuilder> unsoldByCategory = new HashMap<>();

        categorisedCatch.getBuckets().forEach((catchCategory, bucket) -> {
            if (prices.get(catchCategory) == null) {
                unsoldByCategory
                    .computeIfAbsent(catchCategory, key -> Bucket.newBuilder())
                    .add(bucket);
            } else {
                bucket.forEach((species, biomass) -> {
                    getPrice(catchCategory, species).ifPresentOrElse(
                        price -> {
                            soldItems.add(new Sale.Item(
                                catchCategory,
                                species,
                                biomass,
                                price
                            ));
                        },
                        () -> unsoldByCategory
                            .computeIfAbsent(catchCategory, key -> Bucket.newBuilder())
                            .add(species, biomass)
                    );
                });
            }
        });
        final CategorisedCatch unsold = unsoldByCategory.isEmpty()
            ? CategorisedCatch.empty()
            : new CategorisedCatch(
                unsoldByCategory
                    .entrySet()
                    .stream()
                    .collect(toMap(
                        Entry::getKey,
                        entry -> entry.getValue().build()
                    ))
            );
        final Sale sale = new Sale(
            dateTime,
            saleIdSupplier.get(),
            this,
            vessel,
            soldItems,
            unsold
        );
        eventManager.broadcast(sale);
        return sale;
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
        return priceCacheBySpecies.computeIfAbsent(
            species,
            key -> resolvePrice(pricePerSpecies, key)
        );
    }

    public void setPrice(
        final CatchCategory catchCategory,
        final Species species,
        final Price price
    ) {
        prices
            .computeIfAbsent(catchCategory, k -> new HashMap<>())
            .put(species, price);
        priceCache.remove(catchCategory);
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

    @FunctionalInterface
    public interface PriceConsumer {
        void accept(
            CatchCategory catchCategory,
            Species species,
            Price price
        );
    }

}
