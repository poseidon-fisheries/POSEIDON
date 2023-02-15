/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination;

import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.ActionWeightsCache;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.FisherValuesFromFileCache;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.ActionAttractionField;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputFile;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class GravityDestinationStrategyFactory
    implements AlgorithmFactory<GravityDestinationStrategy> {

    private static final FisherValuesFromFileCache<Double> maxTripDurationCache =
        new FisherValuesFromFileCache<Double>() {
            protected Map<Integer, Map<String, Double>> readValues(final Path valuesFile) {
                return recordStream(valuesFile).collect(
                    groupingBy(
                        record -> record.getInt("year"),
                        toMap(
                            record -> record.getString("ves_no"),
                            record -> record.getDouble("max_trip_duration_in_hours")
                        )
                    ));
            }
        };
    // TODO: This is currently EPO specific, as it excludes tiles from the Atlantic, but should
    //  be made configurable.
    private final Predicate<SeaTile> isValidDestination =
        seaTile -> !(seaTile.getGridX() > 72 && seaTile.getBiology() instanceof EmptyLocalBiology);
    private InputFile actionWeightsFile;
    private InputFile maxTripDurationFile;
    public GravityDestinationStrategyFactory() {
    }
    public GravityDestinationStrategyFactory(final InputFile actionWeightsFile, final InputFile maxTripDurationFile) {
        this.actionWeightsFile = actionWeightsFile;
        this.maxTripDurationFile = maxTripDurationFile;
    }

    public static ToDoubleFunction<Fisher> loadMaxTripDuration(final Path maxTripDurationFile) {
        return fisher -> maxTripDurationCache
            .get(
                maxTripDurationFile,
                TARGET_YEAR,
                fisher
            )
            .orElseThrow(() -> new IllegalStateException(
                "No max trip duration known for " + fisher));

    }

    public InputFile getActionWeightsFile() {
        return actionWeightsFile;
    }

    public void setActionWeightsFile(final InputFile actionWeightsFile) {
        this.actionWeightsFile = actionWeightsFile;
    }

    @SuppressWarnings("unused")
    public InputFile getMaxTripDurationFile() {
        return maxTripDurationFile;
    }

    public void setMaxTripDurationFile(final InputFile maxTripDurationFile) {
        this.maxTripDurationFile = maxTripDurationFile;
    }

    @Override
    public GravityDestinationStrategy apply(final FishState fishState) {
        checkNotNull(actionWeightsFile);
        return new GravityDestinationStrategy(
            this::loadActionWeights,
            this::loadMaxTripDuration,
            this.isValidDestination
        );
    }

    private Map<ActionAttractionField, Double> loadActionWeights(
        final Iterable<ActionAttractionField> fields,
        final Fisher fisher
    ) {
        return stream(fields).collect(toImmutableMap(
            identity(),
            field -> ActionWeightsCache.INSTANCE.get(
                actionWeightsFile.get(),
                TARGET_YEAR,
                fisher,
                field.getActionClass()
            )
        ));
    }

    private double loadMaxTripDuration(final Fisher fisher) {
        return maxTripDurationCache
            .get(
                maxTripDurationFile.get(),
                TARGET_YEAR,
                fisher
            )
            .orElseThrow(() -> new IllegalStateException(
                "No max trip duration known for " + fisher));
    }
}
