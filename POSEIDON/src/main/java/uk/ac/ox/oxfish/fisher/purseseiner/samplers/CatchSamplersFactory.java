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

import com.google.common.collect.Ordering;
import com.univocity.parsers.common.record.Record;
import ec.util.MersenneTwisterFast;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.Streams.stream;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.getSetActionClass;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public abstract class CatchSamplersFactory<B extends LocalBiology>
    implements AlgorithmFactory<Map<Class<? extends AbstractSetAction>, CatchSampler<B>>> {

    private Supplier<SpeciesCodes> speciesCodesSupplier;
    private InputPath catchSamplesFile;
    private boolean yearlyReset = false;

    public CatchSamplersFactory() {
    }

    public CatchSamplersFactory(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final InputPath catchSamplesFile
    ) {
        this.speciesCodesSupplier = speciesCodesSupplier;
        this.catchSamplesFile = catchSamplesFile;
    }

    @SuppressWarnings("unused")
    public Supplier<SpeciesCodes> getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodesSupplier(final Supplier<SpeciesCodes> speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    @SuppressWarnings("unused")
    public InputPath getCatchSamplesFile() {
        return catchSamplesFile;
    }

    @SuppressWarnings("unused")
    public void setCatchSamplesFile(final InputPath selectivityFilePath) {
        this.catchSamplesFile = selectivityFilePath;
    }

    @Override
    public Map<Class<? extends AbstractSetAction>, CatchSampler<B>> apply(final FishState fishState) {
        final MersenneTwisterFast rng = checkNotNull(fishState).getRandom();
        return recordStream(catchSamplesFile.get())
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
                    final CatchSampler<B> instance = makeCatchSampler(fishState, entry.getKey(), entry.getValue(), rng);
                    if (yearlyReset)
                        fishState.scheduleEveryYear((Steppable) simState -> instance.reset(), StepOrder.DAWN);
                    return instance;
                }
            ));
    }

    @SuppressWarnings("UnstableApiUsage")
    private Collection<Double> getBiomasses(
        final Record record,
        final GlobalBiology globalBiology
    ) {
        final String[] columnNames = record.getMetaData().headers();
        final SpeciesCodes speciesCodes = speciesCodesSupplier.get();
        return Arrays.stream(columnNames)
            .filter(columnName -> !"set_type".equals(columnName))
            .flatMap(columnName -> stream(
                Optional
                    .of(speciesCodes.getSpeciesName(columnName.toUpperCase()))
                    .map(globalBiology::getSpeciesByCaseInsensitiveName)
                    .map(species -> entry(
                            species.getIndex(),
                            record.getDouble(columnName) * 1000 // convert tonnes to kg
                        )
                    )
            ))
            .collect(toImmutableSortedMap(Ordering.natural(), Entry::getKey, Entry::getValue))
            .values();
    }

    abstract CatchSampler<B> makeCatchSampler(
        final FishState fishState,
        final Class<? extends AbstractSetAction> actionClass,
        final Collection<Collection<Double>> sample,
        final MersenneTwisterFast rng
    );

    @SuppressWarnings("unused")
    public boolean isYearlyReset() {
        return yearlyReset;
    }

    @SuppressWarnings("unused")
    public void setYearlyReset(final boolean yearlyReset) {
        this.yearlyReset = yearlyReset;
    }
}
