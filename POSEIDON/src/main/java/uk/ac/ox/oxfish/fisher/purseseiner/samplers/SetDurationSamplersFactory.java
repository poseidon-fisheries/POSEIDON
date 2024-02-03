package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.univocity.parsers.common.record.Record;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.csv.CsvParserUtil;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.getSetActionClass;

public class SetDurationSamplersFactory
    implements AlgorithmFactory<Map<Class<? extends AbstractSetAction>, DurationSampler>> {

    private final CacheByFile<List<Record>> recordCache =
        new CacheByFile<>(CsvParserUtil::recordList);
    private InputPath setDurationsFile;
    private final CacheByFishState<Map<Class<? extends AbstractSetAction>, DurationSampler>> samplersCache =
        new CacheByFishState<>(
            fishState -> recordCache.apply(setDurationsFile.get()).stream()
                .collect(toImmutableMap(
                    r -> getSetActionClass(r.getString("set_type")),
                    r -> new DurationSampler(
                        fishState.getRandom(),
                        r.getDouble("mean_log_duration_in_hours"),
                        r.getDouble("standard_deviation_log_in_hours")
                    )
                ))
        );

    public SetDurationSamplersFactory() {
    }

    @SuppressWarnings("unused")
    public SetDurationSamplersFactory(final InputPath setDurationsFile) {
        this.setDurationsFile = setDurationsFile;
    }

    @Override
    public Map<Class<? extends AbstractSetAction>, DurationSampler> apply(final FishState fishState) {
        return samplersCache.get(fishState);
    }

    @SuppressWarnings("unused")
    public InputPath getSetDurationsFile() {
        return setDurationsFile;
    }

    @SuppressWarnings("unused")
    public void setSetDurationsFile(final InputPath setDurationsFile) {
        this.setDurationsFile = setDurationsFile;
    }
}
