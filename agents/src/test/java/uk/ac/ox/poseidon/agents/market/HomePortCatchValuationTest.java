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

package uk.ac.ox.poseidon.agents.market;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.catches.CatchCategoriser;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.agents.catches.CategorisedCatch;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.agents.market.MarketGridTest.market;
import static uk.ac.ox.poseidon.agents.market.MarketGridTest.marketGrid;

class HomePortCatchValuationTest {

    private final CatchCategory category = new CatchCategory("C");
    private final Species pricedSpecies = new Species("S1", null, null);
    private final Species unpricedSpecies = new Species("S2", null, null);
    private final CatchCategoriser catchCategoriser =
        bucket -> new CategorisedCatch(Map.of(category, bucket));
    private final Port homePort = mock(Port.class);
    private final Vessel vessel = mock(Vessel.class);
    private final BiomassMarket market =
        new BiomassMarket(homePort, "M", mock(EventManager.class));
    private final HomePortCatchValuation valuation =
        new HomePortCatchValuation(vessel, catchCategoriser);

    @BeforeEach
    void setUp() {
        when(vessel.getHomePort()).thenReturn(homePort);
        givenMarkets(market);
    }

    @Test
    void valuesTheCatchAtHomePortPrices() {
        market.setPrice(category, pricedSpecies, eurosPerKg(2.0));
        assertThat(valuation.applyAsDouble(new Object(), Bucket.of(pricedSpecies, 10.0)))
            .isEqualTo(20.0);
    }

    @Test
    void valuesTheCatchAtTheHomePortsMarketWhenAnotherPortSharesItsCell() {
        final Market otherMarket = market(mock(Port.class));
        when(otherMarket.quote(any())).thenThrow(new AssertionError("quoted the wrong port"));
        givenMarkets(market, otherMarket);
        market.setPrice(category, pricedSpecies, eurosPerKg(2.0));
        assertThat(valuation.applyAsDouble(new Object(), Bucket.of(pricedSpecies, 10.0)))
            .isEqualTo(20.0);
    }

    @Test
    void unpricedSpeciesCountAsZero() {
        market.setPrice(category, pricedSpecies, eurosPerKg(2.0));
        final Bucket bucket = Bucket.of(Map.of(
            pricedSpecies, Biomass.ofKg(10.0),
            unpricedSpecies, Biomass.ofKg(30.0)
        ));
        assertThat(valuation.applyAsDouble(new Object(), bucket)).isEqualTo(20.0);
    }

    @Test
    void noPricesAtAllGiveZero() {
        assertThat(valuation.applyAsDouble(new Object(), Bucket.of(pricedSpecies, 10.0)))
            .isEqualTo(0.0);
    }

    @Test
    void throwsWithoutAHomePort() {
        when(vessel.getHomePort()).thenReturn(null);
        assertThatThrownBy(() ->
            valuation.applyAsDouble(new Object(), Bucket.of(pricedSpecies, 10.0))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void throwsWithoutAMarketForTheHomePort() {
        givenMarkets(market(mock(Port.class)));
        assertThatThrownBy(() ->
            valuation.applyAsDouble(new Object(), Bucket.of(pricedSpecies, 10.0))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void throwsWithMoreThanOneMarketForTheHomePort() {
        final BiomassMarket otherMarket =
            new BiomassMarket(homePort, "N", mock(EventManager.class));
        givenMarkets(market, otherMarket);
        assertThatThrownBy(() ->
            valuation.applyAsDouble(new Object(), Bucket.of(pricedSpecies, 10.0))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void throwsWithMoreThanOneCurrency() {
        market.setPrice(category, pricedSpecies, eurosPerKg(2.0));
        market.setPrice(
            category,
            unpricedSpecies,
            new Price(Money.of(CurrencyUnit.USD, 3.0), KILOGRAM)
        );
        final Bucket bucket = Bucket.of(Map.of(
            pricedSpecies, Biomass.ofKg(10.0),
            unpricedSpecies, Biomass.ofKg(30.0)
        ));
        assertThatThrownBy(() -> valuation.applyAsDouble(new Object(), bucket))
            .isInstanceOf(IllegalStateException.class);
    }

    private void givenMarkets(final Market... markets) {
        final MarketGrid marketGrid = marketGrid(markets);
        when(vessel.getMarketGrid()).thenReturn(marketGrid);
    }

    private static Price eurosPerKg(final double amount) {
        return new Price(Money.of(CurrencyUnit.EUR, amount), KILOGRAM);
    }
}
