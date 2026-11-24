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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
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
    CurrencyUnit.EUR,
    mock(EventManager.class)
  );
  private HomePortCatchValuation valuation;

  @BeforeEach
  void setUp() {
    when(vessel.getHomePort()).thenReturn(Optional.of(homePort));
    valuation = valuationOver(market);
  }

  @Test
  void valuesTheCatchAtHomePortPrices() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    assertThat(
      valuation.by(vessel).applyAsDouble(Bucket.of(pricedSpecies, 10.0))
    ).isEqualTo(20.0);
  }

  @Test
  void valuesTheCatchAtTheHomePortsMarketWhenAnotherPortSharesItsCell() {
    final Market otherMarket = market(mock(Port.class));
    when(otherMarket.quote(any())).thenThrow(
      new AssertionError("quoted the wrong port")
    );
    valuation = valuationOver(market, otherMarket);
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    assertThat(
      valuation.by(vessel).applyAsDouble(Bucket.of(pricedSpecies, 10.0))
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
    assertThat(valuation.by(vessel).applyAsDouble(bucket)).isEqualTo(20.0);
  }

  @Test
  void noPricesAtAllGiveZero() {
    assertThat(
      valuation.by(vessel).applyAsDouble(Bucket.of(pricedSpecies, 10.0))
    ).isEqualTo(0.0);
  }

  @Test
  void aPriceChangeAfterAValuationShowsLater() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    assertThat(valuation.by(vessel).applyAsDouble(bucket)).isEqualTo(20.0);
    now = now.plusHours(1);
    market.setPrice(category, pricedSpecies, eurosPerKg(3.0), now);
    now = now.plusHours(1);
    assertThat(valuation.by(vessel).applyAsDouble(bucket)).isEqualTo(30.0);
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
    assertThat(valuation.by(vessel).applyAsDouble(bucket)).isEqualTo(20.0);
    market.setPrice(category, pricedSpecies, eurosPerKg(3.0), now);
    assertThat(valuation.by(vessel).applyAsDouble(bucket)).isEqualTo(30.0);
  }

  @Test
  void aChangeOfHomePortShows() {
    final Port otherPort = mock(Port.class);
    final BiomassMarket otherMarket = new BiomassMarket(
      otherPort,
      "N",
      CurrencyUnit.EUR,
      mock(EventManager.class)
    );
    valuation = valuationOver(market, otherMarket);
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    otherMarket.setPrice(category, pricedSpecies, eurosPerKg(3.0), now);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    assertThat(valuation.by(vessel).applyAsDouble(bucket)).isEqualTo(20.0);
    when(vessel.getHomePort()).thenReturn(Optional.of(otherPort));
    assertThat(valuation.by(vessel).applyAsDouble(bucket)).isEqualTo(30.0);
  }

  @Test
  void valuesTheCatchForTheVesselGiven() {
    final Port otherPort = mock(Port.class);
    final BiomassMarket otherMarket = new BiomassMarket(
      otherPort,
      "N",
      CurrencyUnit.EUR,
      mock(EventManager.class)
    );
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    otherMarket.setPrice(category, pricedSpecies, eurosPerKg(3.0), now);
    valuation = valuationOver(market, otherMarket);
    final Vessel otherVessel = mock(Vessel.class);
    when(otherVessel.getHomePort()).thenReturn(Optional.of(otherPort));
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    assertThat(valuation.by(vessel).applyAsDouble(bucket)).isEqualTo(20.0);
    assertThat(valuation.by(otherVessel).applyAsDouble(bucket)).isEqualTo(30.0);
  }

  @Test
  void throwsWithoutAHomePort() {
    when(vessel.getHomePort()).thenReturn(Optional.empty());
    assertThatThrownBy(() ->
      valuation.by(vessel).applyAsDouble(Bucket.of(pricedSpecies, 10.0))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("has no modelled home port");
  }

  @Test
  void keepsTenThousandValuesButNotMore() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final List<Bucket> buckets = IntStream.range(0, 10_001)
      .mapToObj(_ -> Bucket.of(pricedSpecies, 1.0))
      .toList();
    now = now.plusHours(1);
    final ToDoubleFunction<Bucket> first = valuation.by(vessel);
    buckets.subList(0, 10_000).forEach(first::applyAsDouble);
    catchesCategorised.set(0);

    valuation.by(vessel).applyAsDouble(buckets.getFirst());
    assertThat(catchesCategorised).hasValue(0);

    first.applyAsDouble(buckets.getLast());
    catchesCategorised.set(0);
    valuation.by(vessel).applyAsDouble(buckets.getFirst());
    assertThat(catchesCategorised).hasValue(1);
  }

  @Test
  void throwsWithoutAMarketForTheHomePort() {
    valuation = valuationOver(market(mock(Port.class)));
    assertThatThrownBy(() ->
      valuation.by(vessel).applyAsDouble(Bucket.of(pricedSpecies, 10.0))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void throwsWithMoreThanOneMarketForTheHomePort() {
    final BiomassMarket otherMarket = new BiomassMarket(
      homePort,
      "N",
      CurrencyUnit.EUR,
      mock(EventManager.class)
    );
    valuation = valuationOver(market, otherMarket);
    assertThatThrownBy(() ->
      valuation.by(vessel).applyAsDouble(Bucket.of(pricedSpecies, 10.0))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void keepsTheValuesOfCatchesFromOnePickToTheNext() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    valuation.by(vessel).applyAsDouble(bucket);
    valuation.by(vessel).applyAsDouble(bucket);

    assertThat(catchesCategorised).hasValue(1);
  }

  @Test
  void forgetsTheValuesOfCatchesOncePastTheLimitAtTheNextPick() {
    market.setPrice(category, pricedSpecies, eurosPerKg(2.0), now);
    final Bucket bucket = Bucket.of(pricedSpecies, 10.0);
    now = now.plusHours(1);
    final ToDoubleFunction<Bucket> forOnePick = valuation.by(vessel);
    forOnePick.applyAsDouble(bucket);
    for (int i = 0; i < 10_000; i++) {
      forOnePick.applyAsDouble(Bucket.of(pricedSpecies, 1.0));
    }
    catchesCategorised.set(0);

    valuation.by(vessel).applyAsDouble(bucket);

    assertThat(catchesCategorised).hasValue(1);
  }

  private HomePortCatchValuation valuationOver(final Market... markets) {
    return new HomePortCatchValuation(
      catchCategoriser,
      () -> now,
      marketGrid(markets)
    );
  }

  private static Price eurosPerKg(final double amount) {
    return new Price(Money.of(CurrencyUnit.EUR, amount), KILOGRAM);
  }
}
