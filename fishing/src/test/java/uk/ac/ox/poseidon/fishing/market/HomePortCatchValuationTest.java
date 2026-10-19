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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.fishing.market.MarketGridTest.market;
import static uk.ac.ox.poseidon.fishing.market.MarketGridTest.marketGrid;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.fishing.catches.CatchCategoriser;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;
import uk.ac.ox.poseidon.fishing.choices.ContextualisedValuation;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.ports.Port;

class HomePortCatchValuationTest {

  private final CatchCategory category = new CatchCategory("C");
  private final Species pricedSpecies = new Species("S1", null, null);
  private final Species unpricedSpecies = new Species("S2", null, null);
  private final AtomicInteger catchesCategorised = new AtomicInteger();
  private final CatchCategoriser catchCategoriser = bucket -> {
    catchesCategorised.incrementAndGet();
    return new CategorisedCatch(Map.of(category, bucket));
  };
  private final Port homePort = mock(Port.class);
  private final Vessel vessel = mock(Vessel.class);
  private LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
  private final BiomassMarket market = new BiomassMarket(
    homePort,
    "M",
    mock(EventManager.class)
  );
  private final HomePortCatchValuation valuation = new HomePortCatchValuation(
    catchCategoriser,
    _ -> now
  );

  @BeforeEach
  void setUp() {
    when(vessel.getHomePort()).thenReturn(homePort);
    givenMarkets(market);
  }

  @Test
  void valuesTheCatchAtHomePortPrices() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    assertThat(
      valuation
        .inContextOf(vessel)
        .valueOf(new Object(), Bucket.of(pricedSpecies, 10.0))
    ).isEqualTo(20.0);
  }

  @Test
  void valuesTheCatchAtTheHomePortsMarketWhenAnotherPortSharesItsCell() {
    final Market otherMarket = market(mock(Port.class));
    when(otherMarket.quote(any())).thenThrow(
      new AssertionError("quoted the wrong port")
    );
    givenMarkets(market, otherMarket);
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    assertThat(
      valuation
        .inContextOf(vessel)
        .valueOf(new Object(), Bucket.of(pricedSpecies, 10.0))
    ).isEqualTo(20.0);
  }

  @Test
  void unpricedSpeciesCountAsZero() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final Bucket bucket = Bucket.of(
      Map.of(
        pricedSpecies,
        Biomass.ofKg(10.0),
        unpricedSpecies,
        Biomass.ofKg(30.0)
      )
    );
    assertThat(
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isEqualTo(20.0);
  }

  @Test
  void noPricesAtAllGiveZero() {
    assertThat(
      valuation
        .inContextOf(vessel)
        .valueOf(new Object(), Bucket.of(pricedSpecies, 10.0))
    ).isEqualTo(0.0);
  }

  @Test
  void aPriceChangeAfterAValuationShowsLater() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    assertThat(
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isEqualTo(20.0);
    now = now.plusHours(1);
    market.setPrice(category, pricedSpecies, eurosPerKg(3.0), now);
    now = now.plusHours(1);
    assertThat(
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isEqualTo(30.0);
  }

  @Test
  void aPriceChangeInTheSameSecondAsAValuationShows() {
    market.setPrice(
      category,
      pricedSpecies,
      eurosPerKg(2.0),
      now.minusHours(1)
    );
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    assertThat(
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isEqualTo(20.0);
    market.setPrice(category, pricedSpecies, eurosPerKg(3.0), now);
    assertThat(
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isEqualTo(30.0);
  }

  @Test
  void aChangeOfHomePortShows() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    assertThat(
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isEqualTo(20.0);
    final Port otherPort = mock(Port.class);
    final BiomassMarket otherMarket = new BiomassMarket(
      otherPort,
      "N",
      mock(EventManager.class)
    );
    otherMarket.setPrice(
      category,
      pricedSpecies,
      eurosPerKg(3.0),
      now.minusHours(1)
    );
    givenMarkets(market, otherMarket);
    when(vessel.getHomePort()).thenReturn(otherPort);
    assertThat(
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isEqualTo(30.0);
  }

  @Test
  void valuesTheCatchForTheVesselGiven() {
    final Port otherPort = mock(Port.class);
    final BiomassMarket otherMarket = new BiomassMarket(
      otherPort,
      "N",
      mock(EventManager.class)
    );
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    otherMarket.setPrice(category, pricedSpecies, eurosPerKg(3.0), now);
    givenMarkets(market, otherMarket);
    final Vessel otherVessel = mock(Vessel.class);
    when(otherVessel.getHomePort()).thenReturn(otherPort);
    final MarketGrid marketGrid = vessel.getMarketGrid();
    when(otherVessel.getMarketGrid()).thenReturn(marketGrid);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    assertThat(
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isEqualTo(20.0);
    assertThat(
      valuation.inContextOf(otherVessel).valueOf(new Object(), bucket)
    ).isEqualTo(30.0);
  }

  @Test
  void throwsWithoutAHomePort() {
    when(vessel.getHomePort()).thenReturn(null);
    assertThatThrownBy(() ->
      valuation
        .inContextOf(vessel)
        .valueOf(new Object(), Bucket.of(pricedSpecies, 10.0))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void throwsWithoutAMarketForTheHomePort() {
    givenMarkets(market(mock(Port.class)));
    assertThatThrownBy(() ->
      valuation
        .inContextOf(vessel)
        .valueOf(new Object(), Bucket.of(pricedSpecies, 10.0))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void throwsWithMoreThanOneMarketForTheHomePort() {
    final BiomassMarket otherMarket = new BiomassMarket(
      homePort,
      "N",
      mock(EventManager.class)
    );
    givenMarkets(market, otherMarket);
    assertThatThrownBy(() ->
      valuation
        .inContextOf(vessel)
        .valueOf(new Object(), Bucket.of(pricedSpecies, 10.0))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void throwsWithMoreThanOneCurrency() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    market.setPrice(
      category,
      unpricedSpecies,
      new Price(Money.of(CurrencyUnit.USD, 3.0), KILOGRAM),
      now
    );
    final Bucket bucket = Bucket.of(
      Map.of(
        pricedSpecies,
        Biomass.ofKg(10.0),
        unpricedSpecies,
        Biomass.ofKg(30.0)
      )
    );
    assertThatThrownBy(() ->
      valuation.inContextOf(vessel).valueOf(new Object(), bucket)
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void keepsTheValuesOfCatchesFromOnePickToTheNext() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    valuation.inContextOf(vessel).valueOf(new Object(), bucket);
    valuation.inContextOf(vessel).valueOf(new Object(), bucket);

    assertThat(catchesCategorised).hasValue(1);
  }

  @Test
  void forgetsTheValuesOfCatchesOncePastTheLimitAtTheNextPick() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    final ContextualisedValuation<Object, Bucket> forOnePick =
      valuation.inContextOf(vessel);
    forOnePick.valueOf(new Object(), bucket);
    for (int i = 0; i < 10_000; i++) {
      forOnePick.valueOf(new Object(), Bucket.of(pricedSpecies, 1.0));
    }
    catchesCategorised.set(0);

    valuation.inContextOf(vessel).valueOf(new Object(), bucket);

    assertThat(catchesCategorised).hasValue(1);
  }

  private void givenMarkets(final Market... markets) {
    final MarketGrid marketGrid = marketGrid(markets);
    when(vessel.getMarketGrid()).thenReturn(marketGrid);
  }

  private static Price eurosPerKg(final double amount) {
    return new Price(Money.of(CurrencyUnit.EUR, amount), KILOGRAM);
  }
}
