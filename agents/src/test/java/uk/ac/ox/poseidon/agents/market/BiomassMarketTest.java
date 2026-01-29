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
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static tech.units.indriya.unit.Units.KILOGRAM;

class BiomassMarketTest {

    @Test
    void getPriceResolvesCoveringSpeciesAndInvalidatesMissCacheOnSetPrice() {
        final CatchCategory category = new CatchCategory("C1");
        final Species baseSpecies = new Species("S", null, null);
        final Species juvenileSpecies = new Species("S", "J", null);
        final Species missingSpecies = new Species("T", null, null);

        final Price basePrice = new Price(Money.of(CurrencyUnit.of("EUR"), 2.0), KILOGRAM);
        final Price missingPrice = new Price(Money.of(CurrencyUnit.of("EUR"), 3.0), KILOGRAM);

        final Map<Species, Price> pricesBySpecies = new HashMap<>();
        pricesBySpecies.put(baseSpecies, basePrice);

        final Map<CatchCategory, Map<Species, Price>> prices = new HashMap<>();
        prices.put(category, pricesBySpecies);

        final BiomassMarket market = new BiomassMarket(
            mock(Port.class),
            "M1",
            prices,
            mock(EventManager.class)
        );

        assertThat(market.getPrice(category, juvenileSpecies)).contains(basePrice);
        assertThat(market.getPrice(category, missingSpecies)).isEmpty();

        market.setPrice(category, missingSpecies, missingPrice);

        assertThat(market.getPrice(category, missingSpecies)).contains(missingPrice);
    }

    @Test
    void forEachPriceIteratesAllEntries() {
        final CatchCategory firstCategory = new CatchCategory("C1");
        final CatchCategory secondCategory = new CatchCategory("C2");
        final Species firstSpecies = new Species("S1", null, null);
        final Species secondSpecies = new Species("S2", null, null);

        final Price firstPrice = new Price(Money.of(CurrencyUnit.of("EUR"), 2.0), KILOGRAM);
        final Price secondPrice = new Price(Money.of(CurrencyUnit.of("USD"), 3.0), KILOGRAM);

        final Map<CatchCategory, Map<Species, Price>> prices = new HashMap<>();
        prices.put(firstCategory, Map.of(firstSpecies, firstPrice));
        prices.put(secondCategory, Map.of(secondSpecies, secondPrice));

        final BiomassMarket market = new BiomassMarket(
            mock(Port.class),
            "M2",
            prices,
            mock(EventManager.class)
        );

        final Set<String> seen = new HashSet<>();
        market.forEachPrice((category, species, price) ->
            seen.add(category.getCode() + ":" + species.getCode() + ":" +
                price.getAmount().getCurrencyUnit().getCode())
        );

        assertThat(seen).containsExactlyInAnyOrder(
            "C1:S1:EUR",
            "C2:S2:USD"
        );
    }
}
