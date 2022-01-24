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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.ActionWeightsCache;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.FisherValuesFromFileCache;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.ActionAttractionField;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class GravityDestinationStrategyFactory
    implements AlgorithmFactory<GravityDestinationStrategy> {

    private static final FisherValuesFromFileCache<Double> maxTripDurationCache =
        new FisherValuesFromFileCache<Double>() {
            protected Map<Integer, Map<String, Double>> readValues(final Path valuesFile) {
                return parseAllRecords(valuesFile).stream().collect(
                    groupingBy(
                        record -> record.getInt("year"),
                        toMap(
                            record -> record.getString("boat_id"),
                            record -> record.getDouble("max_trip_duration_in_hours")
                        )
                    ));
            }
        };
    // TODO: This is currently EPO specific, as it excludes tiles from the Atlantic, but should
    //  be made configurable.
    private final Predicate<SeaTile> isValidDestination =
        seaTile -> !(seaTile.getGridX() > 72 && seaTile.getBiology() instanceof EmptyLocalBiology);
    Path attractionWeightsFile;
    private Path maxTripDurationFile = EpoScenario.INPUT_PATH.resolve("boats.csv");

    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    public void setAttractionWeightsFile(final Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
    }

    @SuppressWarnings("unused")
    public Path getMaxTripDurationFile() {
        return maxTripDurationFile;
    }

    public void setMaxTripDurationFile(final Path maxTripDurationFile) {
        this.maxTripDurationFile = maxTripDurationFile;
    }

    @Override
    public GravityDestinationStrategy apply(final FishState fishState) {
        checkNotNull(attractionWeightsFile);
        return new GravityDestinationStrategy(
            this::loadAttractionWeights,
            this::loadMaxTripDuration,
            this.isValidDestination
        );
    }

    private Map<ActionAttractionField, Double> loadAttractionWeights(
        final Iterable<ActionAttractionField> fields,
        final Fisher fisher
    ) {
        return stream(fields).collect(toImmutableMap(
            identity(),
            field -> ActionWeightsCache.INSTANCE.get(
                attractionWeightsFile,
                TARGET_YEAR,
                fisher,
                field.getActionClass()
            )
        ));
    }

    private double loadMaxTripDuration(final Fisher fisher) {
        return maxTripDurationCache
            .get(maxTripDurationFile, TARGET_YEAR, fisher)
            .orElseThrow(() -> new IllegalStateException(
                "No max trip duration known for " + fisher));
    }

}
