package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import static com.google.common.base.Preconditions.checkState;

public class FadMapFactory implements AlgorithmFactory<FadMap> {

    private final Class<? extends LocalBiology> localBiologyClass;
    private CurrentPatternMapSupplier currentPatternMapSupplier;
    private boolean inputIsMetersPerSecond = true;

    FadMapFactory(
        final Class<? extends LocalBiology> localBiologyClass,
        final CurrentPatternMapSupplier currentPatternMapSupplier
    ) {
        this(localBiologyClass);
        this.currentPatternMapSupplier = currentPatternMapSupplier;
    }

    FadMapFactory(
        final Class<? extends LocalBiology> localBiologyClass
    ) {
        this.localBiologyClass = localBiologyClass;
    }

    @SuppressWarnings("unused")
    public CurrentPatternMapSupplier getCurrentPatternMapSupplier() {
        return currentPatternMapSupplier;
    }

    @SuppressWarnings("unused")
    public void setCurrentPatternMapSupplier(final CurrentPatternMapSupplier currentPatternMapSupplier) {
        this.currentPatternMapSupplier = currentPatternMapSupplier;
    }

    @Override
    public FadMap apply(final FishState fishState) {
        // The CurrentVectorsFactory cache works on the assumption that all cached CurrentVectors
        // objects work with the same number of steps per day (i.e., one). It would be feasible to
        // support different steps per day, but I don't see that happening any time soon.
        checkState(fishState.getStepsPerDay() == CurrentVectorsFactory.STEPS_PER_DAY);
        final NauticalMap nauticalMap = fishState.getMap();
        final CurrentVectors currentVectors = buildCurrentVectors(fishState);
        return new FadMap(
            nauticalMap,
            currentVectors,
            fishState.getBiology(),
            localBiologyClass
        );
    }

    CurrentVectors buildCurrentVectors(final FishState fishState) {
        return CurrentVectorsFactory.INSTANCE.getCurrentVectors(
            fishState.getMap().getMapExtent(),
            currentPatternMapSupplier.get(),
            inputIsMetersPerSecond
        );
    }

    @SuppressWarnings("unused")
    public boolean isInputIsMetersPerSecond() {
        return inputIsMetersPerSecond;
    }

    public void setInputIsMetersPerSecond(final boolean inputIsMetersPerSecond) {
        this.inputIsMetersPerSecond = inputIsMetersPerSecond;
    }
}
