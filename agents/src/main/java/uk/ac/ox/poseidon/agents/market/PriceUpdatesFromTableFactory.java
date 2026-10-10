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
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.utils.Measurements;

import javax.measure.Unit;
import javax.measure.quantity.Mass;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static java.lang.System.Logger.Level.WARNING;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static uk.ac.ox.poseidon.core.utils.Utils.multiStringKey;

/**
 * Turns every row of a price table into a {@link PriceUpdate} on the {@link BiomassMarket} of
 * {@link #marketGrid} whose code the row names, dated at the row's date. Nothing is scheduled
 * here: pass the result to
 * {@link uk.ac.ox.poseidon.core.schedule.Factories#scheduledByDateTime(Factory)} for prices to
 * change over time as the table dictates. The markets must already exist: a row naming a market
 * code not found in {@link #marketGrid} is an error, as is a row whose species code matches none
 * of {@link #species}. A species code that matches only species with life stages gets a generic,
 * life-stage-less {@link Species} created on the fly, since markets commonly price species
 * without distinguishing life stages. Built via {@link Factories#priceUpdatesFromTable}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PriceUpdatesFromTableFactory<S extends Scope>
    extends RelativeScopeFactory<S, List<Entry<LocalDateTime, PriceUpdate>>> {

    private static final System.Logger logger =
        System.getLogger(PriceUpdatesFromTableFactory.class.getName());

    private Factory<? super S, ? extends Table> data;

    private String dateColumn;
    private String marketCodeColumn;
    private String speciesCodeColumn;
    private String categoryCodeColumn;
    private String priceColumn;
    private String currencyColumn;
    private String measurementUnitColumn;

    private Factory<? super S, ? extends MarketGrid> marketGrid;
    private Factory<? super S, ? extends Iterable<? extends Species>> species;

    @Override
    protected List<Entry<LocalDateTime, PriceUpdate>> newInstance(final S scope) {

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

        final Map<String, BiomassMarket> marketsByCode =
            marketGrid
                .get(scope)
                .stream()
                .filter(BiomassMarket.class::isInstance)
                .map(BiomassMarket.class::cast)
                .collect(toMap(BiomassMarket::getCode, identity()));
        final Map<String, CatchCategory> catchCategories = new HashMap<>();

        return data
            .get(scope)
            .stream()
            .map(row -> {
                    final String marketCode = row.getString(marketCodeColumn);
                    final BiomassMarket biomassMarket = marketsByCode.get(marketCode);
                    if (biomassMarket == null) {
                        throw new RuntimeException(
                            "Market " + marketCode + " not found in market grid."
                        );
                    }
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
                            _ -> {
                                logger.log(
                                    WARNING,
                                    "No configured species matches price table species " +
                                        "code {0}; creating a generic (no life stage) " +
                                        "species for it.",
                                    speciesCode
                                );
                                return new Species(speciesCode, null, null);
                            }
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
                            new MarketPrice(
                                biomassMarket,
                                new PriceEntry(
                                    catchCategory,
                                    species,
                                    new Price(money, massUnit)
                                )
                            )
                        )
                    );
                }
            ).collect(toImmutableList());
    }

}
