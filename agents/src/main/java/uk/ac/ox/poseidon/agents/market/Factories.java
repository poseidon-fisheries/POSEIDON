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

import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.List;

public class Factories {

    private Factories() {}

    public static BiomassMarketFactory biomassMarket(
        final Factory<? super SimulationScope, ? extends Port> port,
        final String marketCode,
        final Factory<? super SimulationScope, ? extends List<PriceEntry>> pricesEntries
    ) {
        return new BiomassMarketFactory(port, marketCode, pricesEntries);
    }

    public static BiomassMarketGridFromPriceTableFactory biomassMarketGridFromPriceTable(
        final Factory<? super SimulationScope, ? extends Table> data,
        final String dateColumn,
        final String portCodeColumn,
        final String speciesCodeColumn,
        final String categoryCodeColumn,
        final String priceColumn,
        final String currencyColumn,
        final String measurementUnitColumn,
        final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
        final Factory<? super SimulationScope, ? extends Iterable<? extends Species>> species
    ) {
        return new BiomassMarketGridFromPriceTableFactory(
            data, dateColumn, portCodeColumn, speciesCodeColumn,
            categoryCodeColumn, priceColumn, currencyColumn,
            measurementUnitColumn, portGrid, species
        );
    }

    public static BiomassSaleAccumulatorFactory biomassSaleAccumulator() {
        return new BiomassSaleAccumulatorFactory();
    }

    public static <S extends Scope> MarketGridFactory<S> marketGrid(
        final Factory<? super S, ? extends PortGrid> portGrid,
        final Factory<? super S, ? extends List<? extends Market>> markets
    ) {
        return new MarketGridFactory<>(portGrid, markets);
    }

    public static OneBiomassMarketPerPortFactory oneBiomassMarketPerPort(
        final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
        final Factory<? super SimulationScope, ? extends List<PriceEntry>> pricesEntries
    ) {
        return new OneBiomassMarketPerPortFactory(portGrid, pricesEntries);
    }

    public static <S extends Scope> PriceEntryFactory<S> priceEntry(
        final Factory<? super S, ? extends CatchCategory> catchCategory,
        final Factory<? super S, ? extends Species> species,
        final Factory<? super S, ? extends Price> price
    ) {
        return new PriceEntryFactory<>(catchCategory, species, price);
    }

    public static PriceFactory price(
        final double amount,
        final String currencyUnit,
        final String massUnit
    ) {
        return new PriceFactory(amount, currencyUnit, massUnit);
    }
}
