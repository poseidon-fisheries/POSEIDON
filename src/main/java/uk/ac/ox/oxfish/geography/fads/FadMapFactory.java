package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public class FadMapFactory implements AlgorithmFactory<FadMap> {

    private Map<CurrentPattern, Path> currentFiles = TunaScenario.currentFiles;

    @SuppressWarnings("unused")
    public Map<CurrentPattern, Path> getCurrentFiles() {
        return currentFiles;
    }

    @SuppressWarnings("unused")
    public void setCurrentFiles(Map<CurrentPattern, Path> currentFiles) {
        this.currentFiles = currentFiles;
    }

    @Override
    public FadMap apply(FishState fishState) {
        // The CurrentVectorsFactory cache works on the assumption that all cached CurrentVectors objects
        // work with the same number of steps per day (i.e., one). It would be feasible to support
        // different steps per day, but I don't see that happening any time soon.
        checkState(fishState.getStepsPerDay() == CurrentVectorsFactory.STEPS_PER_DAY);
        NauticalMap nauticalMap = fishState.getMap();
        final CurrentVectors currentVectors =
            CurrentVectorsFactory.INSTANCE.getCurrentVectors(new MapExtent(nauticalMap), currentFiles);
        return new FadMap(nauticalMap, currentVectors, fishState.getBiology());
    }

}
