package uk.ac.ox.oxfish.geography.fads;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class FadMapFactory implements AlgorithmFactory<FadMap> {

    private Map<CurrentPattern, Path> currentFiles;

    /**
     * Empty constructor for YAML loading.
     */
    public FadMapFactory() {
    }

    public FadMapFactory(final Map<CurrentPattern, Path> currentFiles) {
        this.currentFiles = ImmutableMap.copyOf(currentFiles);
    }

    @SuppressWarnings("unused")
    public Map<CurrentPattern, Path> getCurrentFiles() {
        return Collections.unmodifiableMap(currentFiles);
    }

    @SuppressWarnings("unused")
    public void setCurrentFiles(final Map<CurrentPattern, Path> currentFiles) {
        this.currentFiles = ImmutableMap.copyOf(currentFiles);
    }

    @Override
    public FadMap apply(final FishState fishState) {
        // The CurrentVectorsFactory cache works on the assumption that all cached CurrentVectors objects
        // work with the same number of steps per day (i.e., one). It would be feasible to support
        // different steps per day, but I don't see that happening any time soon.
        checkState(fishState.getStepsPerDay() == CurrentVectorsFactory.STEPS_PER_DAY);
        final NauticalMap nauticalMap = fishState.getMap();
        final CurrentVectors currentVectors =
            CurrentVectorsFactory.INSTANCE.getCurrentVectors(
                new MapExtent(nauticalMap),
                currentFiles
            );
        return new FadMap(nauticalMap, currentVectors, fishState.getBiology());
    }

}
