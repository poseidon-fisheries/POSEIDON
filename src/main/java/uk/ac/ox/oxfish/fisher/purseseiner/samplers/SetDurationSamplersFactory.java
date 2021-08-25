package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.fisher.purseseiner.caches.FisherValuesByActionFromFileCache.ActionClass.getSetActionClass;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class SetDurationSamplersFactory
    implements AlgorithmFactory<Map<Class<? extends AbstractSetAction<?>>, DurationSampler>> {

    private Path setDurationsFile = TunaScenario.input("set_durations.csv");

    @Override
    public Map<Class<? extends AbstractSetAction<?>>, DurationSampler> apply(FishState fishState) {
        return parseAllRecords(setDurationsFile)
            .stream()
            .collect(toImmutableMap(
                r -> getSetActionClass(r.getString("set_type")),
                r -> new DurationSampler(
                    fishState.getRandom(),
                    r.getDouble("mean_log_duration_in_hours"),
                    r.getDouble("standard_deviation_log_in_hours")
                )
            ));
    }

    @SuppressWarnings("unused")
    public Path getSetDurationsFile() {
        return setDurationsFile;
    }

    @SuppressWarnings("unused")
    public void setSetDurationsFile(Path setDurationsFile) {
        this.setDurationsFile = setDurationsFile;
    }
}
