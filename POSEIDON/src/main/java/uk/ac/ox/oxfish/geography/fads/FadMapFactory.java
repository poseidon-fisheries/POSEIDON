package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public class FadMapFactory<B extends LocalBiology, F extends AbstractFad<B, F>>
    implements AlgorithmFactory<FadMap<B, F>> {

    private final Class<B> localBiologyClass;
    private final Class<F> fadClass;
    private Map<CurrentPattern, Path> currentFiles;


    private boolean inputIsMetersPerSecond = true;


    FadMapFactory(
        final Class<B> localBiologyClass,
        final Class<F> fadClass,
        final Map<CurrentPattern, Path> currentFiles
    ) {
        this(localBiologyClass, fadClass);
        this.currentFiles = ImmutableMap.copyOf(currentFiles);
    }

    FadMapFactory(
        final Class<B> localBiologyClass,
        final Class<F> fadClass
    ) {
        this.localBiologyClass = localBiologyClass;
        this.fadClass = fadClass;
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
    public FadMap<B, F> apply(final FishState fishState) {
        // The CurrentVectorsFactory cache works on the assumption that all cached CurrentVectors
        // objects work with the same number of steps per day (i.e., one). It would be feasible to
        // support different steps per day, but I don't see that happening any time soon.
        checkState(fishState.getStepsPerDay() == CurrentVectorsFactory.STEPS_PER_DAY);
        final NauticalMap nauticalMap = fishState.getMap();
        final CurrentVectors currentVectors = buildCurrentVectors(fishState);
        return new FadMap<>(
            nauticalMap,
            currentVectors,
            fishState.getBiology(),
            localBiologyClass,
            fadClass
        );
    }

    CurrentVectors buildCurrentVectors(FishState fishState) {
        return CurrentVectorsFactory.INSTANCE.getCurrentVectors(
            fishState.getMap().getMapExtent(),
            currentFiles,
            inputIsMetersPerSecond
        );
    }

    @SuppressWarnings("unused")
    public boolean isInputIsMetersPerSecond() {
        return inputIsMetersPerSecond;
    }

    public void setInputIsMetersPerSecond(boolean inputIsMetersPerSecond) {
        this.inputIsMetersPerSecond = inputIsMetersPerSecond;
    }
}