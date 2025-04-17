package uk.ac.ox.poseidon.agents.tables;

import com.google.common.collect.Table;
import org.joda.money.Money;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import uk.ac.ox.poseidon.agents.market.Sale;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Set;

@SuppressWarnings("rawtypes")
public class MarketSalesListenerTable extends ListenerTable<Sale> {

    public static final String DATE_TIME_COLUMN = "date_time";
    public static final String SALES_ID_COLUMN = "sales_id";
    public static final String MARKET_ID_COLUMN = "market_id";
    public static final String VESSEL_ID_COLUMN = "vessel_id";
    public static final String SPECIES_ID_COLUMN = "species_id";
    public static final String BIOMASS_SOLD_COLUMN = "biomass_sold_in_kg";
    public static final String SALE_VALUE_COLUMN = "sale_value";
    public static final String CURRENCY_COLUMN = "currency";

    private final DateTimeColumn dateTime = DateTimeColumn.create(DATE_TIME_COLUMN);
    private final StringColumn salesId = StringColumn.create(SALES_ID_COLUMN);
    private final StringColumn marketId = StringColumn.create(MARKET_ID_COLUMN);
    private final StringColumn vesselId = StringColumn.create(VESSEL_ID_COLUMN);
    private final StringColumn speciesId = StringColumn.create(SPECIES_ID_COLUMN);
    private final DoubleColumn biomassSold = DoubleColumn.create(BIOMASS_SOLD_COLUMN);
    private final DoubleColumn saleValue = DoubleColumn.create(SALE_VALUE_COLUMN);
    private final StringColumn currency = StringColumn.create(CURRENCY_COLUMN);

    protected MarketSalesListenerTable() {
        super(Sale.class);
        table.addColumns(
            dateTime,
            salesId,
            marketId,
            vesselId,
            speciesId,
            biomassSold,
            saleValue,
            currency
        );
    }

    @Override
    public void receive(Sale sale) {
        @SuppressWarnings("unchecked") final Set<Table.Cell<Species, Content<?>, Money>> cells =
            (Set<Table.Cell<Species, Content<?>, Money>>) (Set<?>) sale.getSold().cellSet();

        for (Table.Cell<Species, Content<?>, Money> cell : cells) {
            dateTime.append(sale.getDateTime());
            salesId.append(sale.getId());
            marketId.append(sale.getMarket().getId());
            vesselId.append(sale.getVessel().getId());
            speciesId.append(cell.getRowKey().getCode());
            biomassSold.append(cell.getColumnKey().asBiomass().asKg());
            saleValue.append(cell.getValue().getAmount().doubleValue());
            currency.append(cell.getValue().getCurrencyUnit().getCode());
        }
    }
}
