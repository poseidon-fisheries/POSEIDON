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

package uk.ac.ox.poseidon.fishing.tables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Table;
import tech.units.indriya.unit.Units;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;
import uk.ac.ox.poseidon.fishing.market.Market;
import uk.ac.ox.poseidon.fishing.market.Price;
import uk.ac.ox.poseidon.fishing.market.Sale;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

final class MarketSalesListenerTableTest {

  private final Simulation simulation = mock(Simulation.class);

  private static Sale sale() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getId()).thenReturn("V1");
    final Market market = mock(Market.class);
    when(market.getCode()).thenReturn("M1");
    final Price price = new Price(
      Money.of(CurrencyUnit.EUR, 2.0),
      Units.KILOGRAM
    );
    return new Sale(
      LocalDateTime.of(2026, 1, 2, 3, 0),
      "S1",
      market,
      vessel,
      List.of(
        new Sale.Item(
          new CatchCategory("C1"),
          new Species("HKE", null, "Hake"),
          Biomass.ofKg(10.0),
          price
        ),
        new Sale.Item(
          new CatchCategory("C2"),
          new Species("MUT", null, "Red mullet"),
          Biomass.ofKg(5.0),
          price
        )
      ),
      CategorisedCatch.empty()
    );
  }

  @Test
  void recordsOneRowPerSaleItem() {
    final MarketSalesListenerTable listener = new MarketSalesListenerTable();

    listener.receive(sale());

    final Table table = listener.get();
    assertThat(table.rowCount()).isEqualTo(2);
    assertThat(table.dateTimeColumn("date_time").asList()).containsOnly(
      LocalDateTime.of(2026, 1, 2, 3, 0)
    );
    assertThat(table.stringColumn("sales_id").asList()).containsOnly("S1");
    assertThat(table.stringColumn("market_id").asList()).containsOnly("M1");
    assertThat(table.stringColumn("vessel_id").asList()).containsOnly("V1");
    assertThat(table.stringColumn("category_code").asList()).containsExactly(
      "C1",
      "C2"
    );
    assertThat(table.stringColumn("species_code").asList()).containsExactly(
      "HKE",
      "MUT"
    );
    assertThat(
      table.doubleColumn("biomass_sold_in_kg").asList()
    ).containsExactly(10.0, 5.0);
    assertThat(table.doubleColumn("sale_value").asList()).containsExactly(
      20.0,
      10.0
    );
    assertThat(table.stringColumn("currency").asList()).containsOnly("EUR");
  }

  @Test
  void factoryBuildsAnEmptyListener() {
    final MarketSalesListenerTable listener =
      new MarketSalesListenerTableFactory().newListener(
        new SimulationScope(simulation)
      );

    assertThat(listener.get().rowCount()).isZero();
    assertThat(listener.get().columnCount()).isEqualTo(9);
  }
}
