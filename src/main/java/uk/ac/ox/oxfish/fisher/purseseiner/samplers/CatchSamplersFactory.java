package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.common.collect.Ordering;
import com.univocity.parsers.common.record.Record;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.Streams.stream;
import static uk.ac.ox.oxfish.fisher.purseseiner.caches.FisherValuesByActionFromFileCache.ActionClasses.getSetActionClass;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class CatchSamplersFactory
    implements AlgorithmFactory<Map<Class<? extends AbstractSetAction>, CatchSampler>> {

    private Path catchSamplesFile;

    @SuppressWarnings("unused")
    public Path getCatchSamplesFile() {
        return catchSamplesFile;
    }

    @SuppressWarnings("unused")
    public void setCatchSamplesFile(Path catchSamplesFile) {
        this.catchSamplesFile = catchSamplesFile;
    }

    @Override
    public Map<Class<? extends AbstractSetAction>, CatchSampler> apply(FishState fishState) {
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
                entry -> new CatchSampler(entry.getValue(), rng)
            ));
    }

    @SuppressWarnings("UnstableApiUsage")
    private Collection<Double> getBiomasses(final Record record, final GlobalBiology globalBiology) {
        String[] columnNames = record.getMetaData().headers();
        return Arrays.stream(columnNames)
            .flatMap(columnName -> stream(
                Optional
                    .ofNullable(TunaScenario.speciesNames.get(columnName.toUpperCase()))
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
}
