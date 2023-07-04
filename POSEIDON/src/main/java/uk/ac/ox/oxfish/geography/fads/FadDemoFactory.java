package uk.ac.ox.oxfish.geography.fads;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A simple algorithm factory to plop an unrealistic fad map and an exogenous fad maker csv;
 * may be changed later, but for now this is primarilly a vehicle to test FAD movement and deployment
 * without the full tuna scenario
 */
public class FadDemoFactory implements AlgorithmFactory<AdditionalStartable> {

    private final ExogenousFadMakerCSVFactory exogenousFadMaker = new ExogenousFadMakerCSVFactory();


    private final FadMapDummyFactory map = new FadMapDummyFactory();


    @Override
    public AdditionalStartable apply(final FishState state) {

        //we are going to put the fad map in right as soon as this is initialized
        //since usually fad maps are needed by many strategies/objects
        //when they start
        final FadMap fadMap = map.apply(state);
        Preconditions.checkState(
            state.getFadMap() == null,
            "There is already a FAD map in the model"
        );
        state.setFadMap(fadMap);
        final AdditionalStartable fadMaker = exogenousFadMaker.apply(state);
        return model -> {
            fadMaker.start(model);
            fadMap.start(state);
        };


    }

    public DoubleParameter getFixedXCurrent() {
        return map.getFixedXCurrent();
    }

    public void setFixedXCurrent(final DoubleParameter fixedXCurrent) {
        map.setFixedXCurrent(fixedXCurrent);
    }

    public DoubleParameter getFixedYCurrent() {
        return map.getFixedYCurrent();
    }

    public void setFixedYCurrent(final DoubleParameter fixedYCurrent) {
        map.setFixedYCurrent(fixedYCurrent);
    }

    public boolean isBiomassOnly() {
        return map.isBiomassOnly();
    }

    public void setBiomassOnly(final boolean biomassOnly) {
        map.setBiomassOnly(biomassOnly);
    }

    public InputPath getPathToFile() {
        return exogenousFadMaker.getDeploymentsFile();
    }

    public void setPathToFile(final InputPath pathToFile) {
        exogenousFadMaker.setDeploymentsFile(pathToFile);
    }

    public AlgorithmFactory<? extends FadInitializer<?, ?>> getFadInitializer() {
        return exogenousFadMaker.getFadInitializer();
    }

    public void setFadInitializer(final CompressedExponentialFadInitializerFactory<?, ?> fadInitializer) {
        exogenousFadMaker.setFadInitializer(fadInitializer);
    }
}
