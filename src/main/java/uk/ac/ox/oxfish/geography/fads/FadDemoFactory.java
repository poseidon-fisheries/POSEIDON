package uk.ac.ox.oxfish.geography.fads;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A simple algorithm factory to plop an unrealistic fad map and an exogenous fad maker csv;
 * may be changed later, but for now this is primarilly a vehicle to test FAD movement and deployment
 * without the full tuna scenario
 */
public class FadDemoFactory implements AlgorithmFactory<AdditionalStartable> {

    private final ExogenousFadMakerFactoryCSV exogenousFadMaker = new ExogenousFadMakerFactoryCSV();


    private final FadMapDummyFactory map = new FadMapDummyFactory();


    @Override
    public AdditionalStartable apply(FishState state) {

        //we are going to put the fad map in right as soon as this is initialized
        //since usually fad maps are needed by many strategies/objects
        //when they start
        FadMap fadMap = map.apply(state);
        Preconditions.checkState(state.getFadMap()==null,
                "There is already a FAD map in the model");
       state.setFadMap(fadMap);
        AdditionalStartable fadMaker = exogenousFadMaker.apply(state);
        return new AdditionalStartable() {
           @Override
           public void start(FishState model) {
               fadMaker.start(model);
               fadMap.start(state);
           }
       };



    }

    public DoubleParameter getFixedXCurrent() {
        return map.getFixedXCurrent();
    }

    public void setFixedXCurrent(DoubleParameter fixedXCurrent) {
        map.setFixedXCurrent(fixedXCurrent);
    }

    public DoubleParameter getFixedYCurrent() {
        return map.getFixedYCurrent();
    }

    public void setFixedYCurrent(DoubleParameter fixedYCurrent) {
        map.setFixedYCurrent(fixedYCurrent);
    }

    public boolean isBiomassOnly() {
        return map.isBiomassOnly();
    }

    public void setBiomassOnly(boolean biomassOnly) {
        map.setBiomassOnly(biomassOnly);
    }

    public String getPathToFile() {
        return exogenousFadMaker.getPathToFile();
    }

    public void setPathToFile(String pathToFile) {
        exogenousFadMaker.setPathToFile(pathToFile);
    }

    public FadInitializerFactory getFadInitializer() {
        return exogenousFadMaker.getFadInitializer();
    }

    public void setFadInitializer(FadInitializerFactory fadInitializer) {
        exogenousFadMaker.setFadInitializer(fadInitializer);
    }
}
