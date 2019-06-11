package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentMaps;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;

import static uk.ac.ox.oxfish.geography.currents.CurrentsMapFactory.makeCurrentsMaps;

public class FadMapFactory implements AlgorithmFactory<FadMap> {

    private Path currentsVectorFilePath;

    public FadMapFactory(Path currentsVectorFilePath) {
        this.currentsVectorFilePath = currentsVectorFilePath;
    }

    @SuppressWarnings("unused")
    public Path getCurrentsVectorFilePath() {
        return currentsVectorFilePath;
    }

    @SuppressWarnings("unused")
    public void setCurrentsVectorFilePath(Path currentsVectorFilePath) {
        this.currentsVectorFilePath = currentsVectorFilePath;
    }

    @Override public FadMap apply(FishState fishState) {
        final NauticalMap nauticalMap = fishState.getMap();
        final CurrentMaps currentMaps = makeCurrentsMaps(nauticalMap, currentsVectorFilePath);
        return new FadMap(nauticalMap, currentMaps, fishState.getBiology());
    }

}
