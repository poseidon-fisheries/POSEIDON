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
import sim.engine.Steppable;
import sim.util.Int2D;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BiomassMarketGridFromPriceTableFactoryTest {

    @Test
    void stagedConfiguredSpeciesUseSingleGenericPriceEntry() {
        final BiomassMarketGridFromPriceTableFactory factory = factory(
            priceTable("HKE"),
            List.of(
                new Species("HKE", "adult", "Hake"),
                new Species("HKE", "juvenile", "Hake")
            )
        );

        final MarketGrid marketGrid = factory.get(scopeApplyingScheduledPriceUpdates());
        final BiomassMarket market = (BiomassMarket) marketGrid
            .stream()
            .findFirst()
            .orElseThrow();
        final CatchCategory catchCategory = new CatchCategory("Fresh - Whole");

        assertThat(market.getPrices().get(catchCategory))
            .containsOnlyKeys(new Species("HKE", null, null))
            .doesNotContainKeys(
                new Species("HKE", "adult", null),
                new Species("HKE", "juvenile", null)
            );
    }

    @Test
    void rejectsUnknownPriceTableSpecies() {
        final BiomassMarketGridFromPriceTableFactory factory = factory(
            priceTable("XYZ"),
            List.of(new Species("HKE", "adult", "Hake"))
        );

        assertThatThrownBy(() -> factory.get(scopeApplyingScheduledPriceUpdates()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Species XYZ not found.");
    }

    private static BiomassMarketGridFromPriceTableFactory factory(
        final Table priceTable,
        final List<? extends Species> species
    ) {
        final PortGrid portGrid = portGrid();
        return new BiomassMarketGridFromPriceTableFactory(
            _ -> priceTable,
            "date",
            "market_code",
            "species_code",
            "category_code",
            "price",
            "currency",
            "measurement_unit",
            _ -> portGrid,
            _ -> species
        );
    }

    private static Table priceTable(final String speciesCode) {
        return Table
            .create("prices")
            .addColumns(
                DateColumn.create("date", LocalDate.of(2026, 1, 1)),
                StringColumn.create("market_code", "M1"),
                StringColumn.create("species_code", speciesCode),
                StringColumn.create("category_code", "Fresh - Whole"),
                DoubleColumn.create("price", 12.5),
                StringColumn.create("currency", "EUR"),
                StringColumn.create("measurement_unit", "kg")
            );
    }

    private static PortGrid portGrid() {
        final Port port = mock(Port.class);
        final ModelGrid modelGrid = mock(ModelGrid.class);
        final PortGrid portGrid = mock(PortGrid.class);
        when(modelGrid.getGridWidth()).thenReturn(1);
        when(modelGrid.getGridHeight()).thenReturn(1);
        when(portGrid.getModelGrid()).thenReturn(modelGrid);
        when(portGrid.getObject("M1")).thenReturn(Optional.of(port));
        when(portGrid.getLocation(port)).thenReturn(new Int2D(0, 0));
        return portGrid;
    }

    private static SimulationScope scopeApplyingScheduledPriceUpdates() {
        final Simulation simulation = mock(Simulation.class);
        final TemporalSchedule temporalSchedule = mock(TemporalSchedule.class);
        when(simulation.getTemporalSchedule()).thenReturn(temporalSchedule);
        doAnswer(invocation -> {
            final Collection<? extends Entry<LocalDateTime, ? extends Steppable>> updates =
                invocation.getArgument(0);
            updates.forEach(update -> update.getValue().step(simulation));
            return null;
        }).when(temporalSchedule).scheduleByDateTime(any());
        return new SimulationScope(simulation);
    }
}
