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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.core.utils.Measurements;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import javax.measure.Unit;
import javax.measure.quantity.Mass;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.Streams.stream;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static uk.ac.ox.poseidon.core.utils.Utils.multiStringKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BiomassMarketGridFromPriceTableFactory
    extends SimulationScopeFactory<MarketGrid> {

    private static final System.Logger logger =
        System.getLogger(BiomassMarketGridFromPriceTableFactory.class.getName());

    private Factory<? super SimulationScope, ? extends Table> data;

    private String dateColumn;
    private String portCodeColumn;
    private String speciesCodeColumn;
    private String categoryCodeColumn;
    private String priceColumn;
    private String currencyColumn;
    private String measurementUnitColumn;

    private Factory<? super SimulationScope, ? extends PortGrid> portGrid;
    private Factory<? super SimulationScope, ? extends Iterable<? extends Species>> species;

    @Override
    protected MarketGrid newInstance(final SimulationScope scope) {

        final List<? extends Species> configuredSpecies =
            stream(this.species.get(scope)).toList();
        final Map<String, Species> speciesByKey =
            configuredSpecies
                .stream()
                .collect(toMap(Species::getKey, identity()));
        final Set<String> speciesCodes =
            configuredSpecies
                .stream()
                .map(Species::getCode)
                .collect(toSet());

        final PortGrid portGrid = this.portGrid.get(scope);
        final Map<String, BiomassMarket> markets = new HashMap<>();
        final Map<String, CatchCategory> catchCategories = new HashMap<>();

        final List<Entry<LocalDateTime, PriceUpdate>> priceUpdatesByDate =
            data.get(scope)
                .stream()
                .map(row -> {
                        final BiomassMarket biomassMarket = markets.computeIfAbsent(
                            row.getString(portCodeColumn),
                            portCode -> {
                                final Port port =
                                    portGrid.getObject(portCode).orElseThrow(() ->
                                        new RuntimeException(
                                            "Port " + portCode + " not found in port grid."
                                        )
                                    );
                                return new BiomassMarket(
                                    port,
                                    portCode,
                                    Map.of(),
                                    scope.getSimulation().getEventManager()
                                );
                            }
                        );
                        final String speciesCode = row.getString(speciesCodeColumn);
                        if (!speciesCodes.contains(speciesCode)) {
                            throw new RuntimeException(
                                "Species " + speciesCode + " not found."
                            );
                        }

                        final Species species =
                            // Use the existing species definition if we have it, but otherwise
                            // create a new species object for that particular price. The latter
                            // case will be common when we have species with life stages in the
                            // simulation but the market only deals with generic species.
                            speciesByKey.computeIfAbsent(
                                multiStringKey(speciesCode, null),
                                _ -> new Species(speciesCode, null, null)
                            );

                        final CatchCategory catchCategory =
                            catchCategories.computeIfAbsent(
                                row.getString(categoryCodeColumn),
                                CatchCategory::new
                            );
                        final CurrencyUnit currencyUnit =
                            CurrencyUnit.of(row.getString(currencyColumn));
                        final Unit<Mass> massUnit =
                            Measurements.parseMassUnit(row.getString(measurementUnitColumn));
                        final Money money = Money.of(
                            currencyUnit,
                            row.getDouble(priceColumn),
                            HALF_EVEN
                        );
                        final LocalDateTime localDateTime = row.getDate(dateColumn).atStartOfDay();
                        return entry(
                            localDateTime,
                            new PriceUpdate(
                                biomassMarket,
                                new PriceEntry(
                                    catchCategory,
                                    species,
                                    new Price(money, massUnit)
                                )
                            )
                        );
                    }
                ).toList();
        scope.getSimulation().getTemporalSchedule().scheduleByDateTime(priceUpdatesByDate);

        return new ImmutableMarketGrid(
            portGrid.getModelGrid(),
            markets.values().stream().collect(toMap(
                identity(),
                market -> portGrid.getLocation(market.getPort())
            ))
        );
    }

}
