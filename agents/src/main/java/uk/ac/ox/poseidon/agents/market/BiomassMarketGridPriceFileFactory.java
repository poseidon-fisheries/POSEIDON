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

package uk.ac.ox.poseidon.agents.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.utils.Measurements;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import javax.measure.Unit;
import javax.measure.quantity.Mass;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;
import static java.lang.System.Logger.Level.ERROR;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.stream.Collectors.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BiomassMarketGridPriceFileFactory
    extends SimulationScopeFactory<BiomassMarketGrid> {

    private static final System.Logger logger =
        System.getLogger(BiomassMarketGridPriceFileFactory.class.getName());

    private Factory<? extends Path> path;

    private String dateColumn;
    private String marketCodeColumn;
    private String speciesCodeColumn;
    private String categoryCodeColumn;
    private String priceColumn;
    private String currencyColumn;
    private String measurementUnitColumn;

    // TODO: this a temporary filter that will only work as long as we only have PS vessels.
    //  We need to find a way to propagate the catch method all the way to the market and then
    //  deal with different categories of prices. See:
    //  https://github.com/Official-EwE/SURIMI-project/issues/96
    private String categoryCodeFilter;

    // TODO: similarly, we need to adapt the class to schedule prices updates instead of filtering
    //  on a particular date
    private Factory<? extends LocalDate> dateFilter;

    private Factory<? extends PortGrid> portGrid;
    private Factory<? extends Iterable<? extends Species>> species;

    @Override
    protected BiomassMarketGrid newInstance(final Simulation simulation) {

        final Map<String, List<Species>> speciesByCode =
            stream(this.species.get(simulation))
                .collect(groupingBy(Species::getCode));

        final PortGrid portGrid = this.portGrid.get(simulation);

        final File file = path.get(simulation).toFile();
        // I know. I know. This is ridiculous.
        return Table.read().file(file).stream()
            .filter(row -> row.getString(categoryCodeColumn).equals(categoryCodeFilter))
            .filter(row -> dateFilter.get(simulation).isEqual(row.getDate(dateColumn)))
            .flatMap(row ->
                parse(
                    file,
                    row,
                    marketCodeColumn,
                    id -> portGrid.getObject(id).orElse(null),
                    "port code"
                ).flatMap(port ->
                    parse(
                        file, row, speciesCodeColumn, speciesByCode::get, "species code"
                    ).flatMap(speciesList ->
                        parse(
                            file, row, currencyColumn, CurrencyUnit::of, "currency"
                        ).flatMap(currencyUnit ->
                            parse(
                                file,
                                row,
                                measurementUnitColumn,
                                Measurements::parseMassUnit,
                                "measurement unit"
                            ).flatMap(measurementUnit ->
                                speciesList.stream().map(species ->
                                    new PriceEntry(
                                        port,
                                        species,
                                        Money.of(
                                            currencyUnit,
                                            row.getDouble(priceColumn),
                                            HALF_EVEN
                                        ),
                                        measurementUnit.asType(Mass.class)
                                    )
                                )
                            )
                        )
                    )
                )
            )
            .collect(
                collectingAndThen(
                    groupingBy(
                        PriceEntry::port,
                        toMap(
                            PriceEntry::species,
                            priceEntry -> new Price(
                                priceEntry.price(),
                                priceEntry.unit()
                            )
                        )
                    ),
                    priceBySpeciesByPort -> makeMarketGrid(
                        portGrid, priceBySpeciesByPort,
                        simulation.getEventManager()
                    )
                )
            );
    }

    private BiomassMarketGrid makeMarketGrid(
        final PortGrid portGrid,
        final Map<Port, Map<Species, Price>> priceBySpeciesByPort,
        final EventManager eventManager
    ) {
        final BiomassMarketGrid marketGrid = new BiomassMarketGrid(portGrid);
        priceBySpeciesByPort.forEach((port, priceBySpecies) -> {
            marketGrid.addMarket(
                new BiomassMarket(
                    port.getCode(),
                    priceBySpecies,
                    eventManager
                ),
                port
            );
        });
        return marketGrid;
    }

    private <T> Stream<T> parse(
        final File file,
        final Row row,
        final String columnName,
        final Function<String, T> parser,
        final String description
    ) {
        try {
            final String value = row.getString(columnName);
            return Optional
                .ofNullable(parser.apply(value))
                .or(() -> {
                    logger.log(ERROR, "{0} is not a valid {1}.", value, description);
                    return Optional.empty();
                })
                .stream();
        } catch (final Exception e) {
            logger.log(
                ERROR, "Error parsing row {0}\nwhile reading {1}\n{2}",
                row,
                file,
                e.getMessage()
            );
            return Stream.empty();
        }
    }

    private record PriceEntry(
        Port port,
        Species species,
        Money price,
        Unit<Mass> unit
    ) {}

}
