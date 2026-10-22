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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.fishing.market.Factories.initialPrices;
import static uk.ac.ox.poseidon.fishing.market.Factories.marketPrices;
import static uk.ac.ox.poseidon.fishing.market.Factories.uniformMarketPrices;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

class InitialPricesFactoryTest {

  private static final CatchCategory CATEGORY = new CatchCategory("C");
  private static final Species HKE = new Species("HKE", null, "Hake");

  private final Simulation simulation = mock(Simulation.class);
  private final TemporalSchedule schedule = mock(TemporalSchedule.class);

  @BeforeEach
  void setUp() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(simulation.getTemporalSchedule()).thenReturn(schedule);
    when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));
  }

  @Test
  void appliesMarketSpecificPricesWhenBuilt() {
    final BiomassMarket m1 = market("M1");
    final BiomassMarket m2 = market("M2");

    initialPrices(
      marketPrices(_ -> m1, _ -> List.of(entry(1.0))),
      marketPrices(_ -> m2, _ -> List.of(entry(2.0)))
    ).get(scope());

    assertThat(amount(m1)).isEqualTo(1.0);
    assertThat(amount(m2)).isEqualTo(2.0);
  }

  @Test
  void appliesUniformPricesWhenBuilt() {
    final BiomassMarket m1 = market("M1");
    final BiomassMarket m2 = market("M2");
    final ModelGrid modelGrid = mock(ModelGrid.class);
    when(modelGrid.getGridWidth()).thenReturn(2);
    when(modelGrid.getGridHeight()).thenReturn(1);
    final MarketGrid marketGrid = new ImmutableMarketGrid(
      modelGrid,
      Map.of(m1, new Cell(0, 0), m2, new Cell(1, 0))
    );

    final List<MarketPrice> applied = initialPrices(
      uniformMarketPrices(_ -> marketGrid, _ -> List.of(entry(3.0)))
    ).get(scope());

    assertThat(applied).hasSize(2);
    assertThat(amount(m1)).isEqualTo(3.0);
    assertThat(amount(m2)).isEqualTo(3.0);
  }

  private static PriceEntry entry(final double amount) {
    return new PriceEntry(
      CATEGORY,
      HKE,
      new Price(Money.of(CurrencyUnit.of("EUR"), amount), KILOGRAM)
    );
  }

  private static double amount(final BiomassMarket market) {
    return market
      .getPrice(CATEGORY, HKE)
      .orElseThrow()
      .getAmount()
      .getAmount()
      .doubleValue();
  }

  private static BiomassMarket market(final String code) {
    return new BiomassMarket(
      mock(Port.class),
      code,
      CurrencyUnit.EUR,
      mock(EventManager.class)
    );
  }

  private SimulationScope scope() {
    return new SimulationScope(simulation);
  }
}
