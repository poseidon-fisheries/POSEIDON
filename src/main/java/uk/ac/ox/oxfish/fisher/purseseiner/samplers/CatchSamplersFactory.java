/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.Streams.stream;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.getSetActionClass;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.collect.Ordering;
import com.univocity.parsers.common.record.Record;
import ec.util.MersenneTwisterFast;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class CatchSamplersFactory<B extends LocalBiology>
    implements AlgorithmFactory<Map<Class<? extends AbstractSetAction<?>>, CatchSampler<B>>> {

    private final SpeciesCodes speciesCodes = EpoScenario.speciesCodesSupplier.get();
    private Path catchSamplesFile = INPUT_PATH.resolve("set_samples.csv");

    private boolean yearlyReset = false;

    @SuppressWarnings("unused")
    public Path getCatchSamplesFile() {
        return catchSamplesFile;
    }

    @SuppressWarnings("unused")
    public void setCatchSamplesFile(final Path selectivityFilePath) {
        this.catchSamplesFile = selectivityFilePath;
    }

    @Override
    public Map<Class<? extends AbstractSetAction<?>>, CatchSampler<B>> apply(final FishState fishState) {
        final MersenneTwisterFast rng = checkNotNull(fishState).getRandom();
        return parseAllRecords(catchSamplesFile)
            .stream()
            .collect(toImmutableListMultimap(
                r -> getSetActionClass(r.getString("set_type")),
                r -> getBiomasses(r, fishState.getBiology())
            ))
            .asMap()
            .entrySet()
            .stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> {
                    CatchSampler<B> instance = makeCatchSampler(entry.getKey(), entry.getValue(), rng);
                    if(yearlyReset)
                        fishState.scheduleEveryYear((Steppable) simState -> instance.reset(), StepOrder.DAWN);
                    return instance;
                }
            ));
    }

    abstract CatchSampler<B> makeCatchSampler(
        final Class<? extends AbstractSetAction<?>> actionClass,
        final Collection<Collection<Double>> sample,
        final MersenneTwisterFast rng
    );

    @SuppressWarnings("UnstableApiUsage")
    private Collection<Double> getBiomasses(
        final Record record,
        final GlobalBiology globalBiology
    ) {
        final String[] columnNames = record.getMetaData().headers();
        return Arrays.stream(columnNames)
            .filter(columnName -> !"set_type".equals(columnName))
            .flatMap(columnName -> stream(
                Optional
                    .of(speciesCodes.getSpeciesName(columnName.toUpperCase()))
                    .map(globalBiology::getSpecie)
                    .map(species -> entry(
                        species.getIndex(),
                        record.getDouble(columnName) * 1000 // convert tonnes to kg
                        )
                    )
            ))
            .collect(toImmutableSortedMap(Ordering.natural(), Entry::getKey, Entry::getValue))
            .values();
    }


    public boolean isYearlyReset() {
        return yearlyReset;
    }

    public void setYearlyReset(boolean yearlyReset) {
        this.yearlyReset = yearlyReset;
    }
}
