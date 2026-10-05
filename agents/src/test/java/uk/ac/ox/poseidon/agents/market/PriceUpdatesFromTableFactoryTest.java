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

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PriceUpdatesFromTableFactoryTest {

    private static final CatchCategory CATCH_CATEGORY = new CatchCategory("Fresh - Whole");

    @Test
    void setsPricesOnExistingMarket() {
        final BiomassMarket market = market("M1");
        final PriceUpdatesFromTableFactory factory = factory(
            priceTable("M1", "HKE"),
            marketGrid(market),
            List.of(new Species("HKE", null, "Hake"))
        );

        applyAll(factory);

        assertThat(market.getPrice(CATCH_CATEGORY, new Species("HKE", null, null)))
            .hasValueSatisfying(price ->
                assertThat(price.getAmount().getAmount().doubleValue()).isEqualTo(12.5)
            );
    }

    @Test
    void stagedConfiguredSpeciesUseSingleGenericPriceEntry() {
        final BiomassMarket market = market("M1");
        final PriceUpdatesFromTableFactory factory = factory(
            priceTable("M1", "HKE"),
            marketGrid(market),
            List.of(
                new Species("HKE", "adult", "Hake"),
                new Species("HKE", "juvenile", "Hake")
            )
        );

        applyAll(factory);

        assertThat(market.getPrices().get(CATCH_CATEGORY))
            .containsOnlyKeys(new Species("HKE", null, null))
            .doesNotContainKeys(
                new Species("HKE", "adult", null),
                new Species("HKE", "juvenile", null)
            );
    }

    @Test
    void rejectsUnknownPriceTableSpecies() {
        final PriceUpdatesFromTableFactory factory = factory(
            priceTable("M1", "XYZ"),
            marketGrid(market("M1")),
            List.of(new Species("HKE", "adult", "Hake"))
        );

        assertThatThrownBy(() -> factory.get(scope()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Species XYZ not found.");
    }

    @Test
    void rejectsUnknownMarket() {
        final PriceUpdatesFromTableFactory factory = factory(
            priceTable("M2", "HKE"),
            marketGrid(market("M1")),
            List.of(new Species("HKE", null, "Hake"))
        );

        assertThatThrownBy(() -> factory.get(scope()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Market M2 not found in market grid.");
    }

    private static PriceUpdatesFromTableFactory factory(
        final Table priceTable,
        final MarketGrid marketGrid,
        final List<? extends Species> species
    ) {
        return new PriceUpdatesFromTableFactory(
            _ -> priceTable,
            "date",
            "market_code",
            "species_code",
            "category_code",
            "price",
            "currency",
            "measurement_unit",
            _ -> marketGrid,
            _ -> species
        );
    }

    private static Table priceTable(final String marketCode, final String speciesCode) {
        return Table
            .create("prices")
            .addColumns(
                DateColumn.create("date", LocalDate.of(2026, 1, 1)),
                StringColumn.create("market_code", marketCode),
                StringColumn.create("species_code", speciesCode),
                StringColumn.create("category_code", CATCH_CATEGORY.getCode()),
                DoubleColumn.create("price", 12.5),
                StringColumn.create("currency", "EUR"),
                StringColumn.create("measurement_unit", "kg")
            );
    }

    private static BiomassMarket market(final String code) {
        return new BiomassMarket(mock(Port.class), code, mock(EventManager.class));
    }

    private static MarketGrid marketGrid(final BiomassMarket market) {
        final ModelGrid modelGrid = mock(ModelGrid.class);
        when(modelGrid.getGridWidth()).thenReturn(1);
        when(modelGrid.getGridHeight()).thenReturn(1);
        return new ImmutableMarketGrid(modelGrid, Map.of(market, new Int2D(0, 0)));
    }

    @Test
    void datesEachUpdateAtItsRowDate() {
        final PriceUpdatesFromTableFactory factory = factory(
            priceTable("M1", "HKE"),
            marketGrid(market("M1")),
            List.of(new Species("HKE", null, "Hake"))
        );

        assertThat(factory.get(scope()))
            .extracting(Entry::getKey)
            .containsExactly(LocalDate.of(2026, 1, 1).atStartOfDay());
    }

    private static void applyAll(final PriceUpdatesFromTableFactory factory) {
        factory.get(scope()).forEach(update -> update.getValue().step(null));
    }

    private static SimulationScope scope() {
        return new SimulationScope(mock(Simulation.class));
    }
}
