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

package uk.ac.ox.poseidon.agents.tables;

import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import uk.ac.ox.poseidon.agents.market.Sale;

/**
 * A {@link ListenerTable} that records one row per {@link Sale.Item} in every {@link Sale}: the
 * sale's date/time, ID, market and vessel, and the item's category, species, biomass sold and
 * sale value.
 */
@SuppressWarnings("rawtypes")
public class MarketSalesListenerTable extends ListenerTable<Sale> {

    /** Name of the sale date/time column. */
    public static final String DATE_TIME_COLUMN = "date_time";
    /** Name of the sale-ID column. */
    public static final String SALES_ID_COLUMN = "sales_id";
    /** Name of the market-ID column. */
    public static final String MARKET_ID_COLUMN = "market_id";
    /** Name of the vessel-ID column. */
    public static final String VESSEL_ID_COLUMN = "vessel_id";
    /** Name of the sale item's category-code column. */
    public static final String CATEGORY_CODE_COLUMN = "category_code";
    /** Name of the sale item's species-code column. */
    public static final String SPECIES_CODE_COLUMN = "species_code";
    /** Name of the sale item's biomass-sold (in kg) column. */
    public static final String BIOMASS_SOLD_COLUMN = "biomass_sold_in_kg";
    /** Name of the sale item's sale-value column. */
    public static final String SALE_VALUE_COLUMN = "sale_value";
    /** Name of the sale item's currency column. */
    public static final String CURRENCY_COLUMN = "currency";

    private final DateTimeColumn dateTime = DateTimeColumn.create(DATE_TIME_COLUMN);
    private final StringColumn salesId = StringColumn.create(SALES_ID_COLUMN);
    private final StringColumn marketId = StringColumn.create(MARKET_ID_COLUMN);
    private final StringColumn vesselId = StringColumn.create(VESSEL_ID_COLUMN);
    private final StringColumn categoryCode = StringColumn.create(CATEGORY_CODE_COLUMN);
    private final StringColumn speciesCode = StringColumn.create(SPECIES_CODE_COLUMN);
    private final DoubleColumn biomassSold = DoubleColumn.create(BIOMASS_SOLD_COLUMN);
    private final DoubleColumn saleValue = DoubleColumn.create(SALE_VALUE_COLUMN);
    private final StringColumn currency = StringColumn.create(CURRENCY_COLUMN);

    MarketSalesListenerTable() {
        super(Sale.class);
        table.addColumns(
            dateTime,
            salesId,
            marketId,
            vesselId,
            categoryCode,
            speciesCode,
            biomassSold,
            saleValue,
            currency
        );
    }

    /** Appends one row per {@link Sale.Item} in {@code sale} to {@link #table}. */
    @Override
    public void receive(final Sale sale) {
        sale.getItems().forEach(item -> {
            dateTime.append(sale.getDateTime());
            salesId.append(sale.getId());
            marketId.append(sale.getMarket().getCode());
            vesselId.append(sale.getVessel().getId());
            categoryCode.append(item.getCategory().getCode());
            speciesCode.append(item.getSpecies().getCode());
            biomassSold.append(item.getContent().asBiomass().asKg());
            saleValue.append(item.getSaleValue().getAmount().doubleValue());
            currency.append(item.getSaleValue().getCurrencyUnit().getCode());
        });
    }
}
