package uk.ac.ox.poseidon.datasets.adaptors.rowproviders;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;

import java.util.Map;
import java.util.function.Function;

public class PurseSeinerEventsDatasetFactory extends RowProviderDatasetFactory {
    @Override
    Map<String, Function<FishState, RowProvider>> getRowProviderFactories() {
        return ImmutableMap.of(
            "Actions", PurseSeineActionsLogger::new,
            "Trips", PurseSeineTripLogger::new
        );
    }

    @Override
    public String getDatasetName() {
        return "Purse-seiner events";
    }

    @Override
    public boolean test(final Object o) {
        return o instanceof FishState &&
            ((FishState) o).getScenario() instanceof EpoScenario;
    }
}
