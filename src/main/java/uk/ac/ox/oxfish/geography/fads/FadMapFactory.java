package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory.makeCurrentVectors;

public class FadMapFactory implements AlgorithmFactory<FadMap> {

    @Override public FadMap apply(FishState fishState) {
        final NauticalMap nauticalMap = fishState.getMap();
        final CurrentVectors currentVectors = makeCurrentVectors(nauticalMap, fishState.getStepsPerDay());
        return new FadMap(nauticalMap, currentVectors, fishState.getBiology());
    }

}
