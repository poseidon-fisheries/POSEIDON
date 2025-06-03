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

import com.google.common.collect.ImmutableTable;
import lombok.Getter;
import lombok.ToString;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.utils.IdSupplier;
import uk.ac.ox.poseidon.core.utils.PrefixedIdSupplier;

import javax.measure.Unit;
import javax.measure.quantity.Mass;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public class BiomassMarket implements Market<Biomass> {

    private final String id;
    private final Map<Species, Price> prices;
    private final IdSupplier saleIdSupplier;
    private final EventManager eventManager;

    BiomassMarket(
        final String id,
        final Map<Species, Price> prices,
        final EventManager eventManager
    ) {
        this.id = id;
        this.prices = new HashMap<>(prices);
        this.saleIdSupplier = new PrefixedIdSupplier(id);
        this.eventManager = eventManager;
    }

    @Override
    public Sale<Biomass> sell(
        final Vessel vessel,
        final Map<String, Bucket<? extends Biomass>> buckets,
        final LocalDateTime dateTime
    ) {
        final ImmutableTable.Builder<Species, Biomass, Money> sold = ImmutableTable.builder();
        final Bucket.Builder<Biomass> unsold = Bucket.newBuilder();

        for (final Map.Entry<Species, ? extends Biomass> entry : bucket.getMap().entrySet()) {
            final Species species = entry.getKey();
            final Biomass biomass = entry.getValue();
            final Price price = prices.get(species);
            if (price == null) {
                unsold.add(species, biomass);
            } else {
                final Money salePrice =
                    price.amount.multipliedBy(
                        biomass.as(price.biomassUnit),
                        RoundingMode.DOWN
                    );
                sold.put(species, biomass, salePrice);
            }
        }
        final BiomassSale sale = new BiomassSale(
            dateTime,
            saleIdSupplier.nextId(),
            this,
            vessel,
            sold.build(),
            unsold.build()
        );
        eventManager.broadcast(sale);
        return sale;
    }

    public void setPrice(
        final Species species,
        final Price price
    ) {
        prices.put(species, price);
    }

    public record Price(Money amount, Unit<Mass> biomassUnit) {}

}
