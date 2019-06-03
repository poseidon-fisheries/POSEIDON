package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentMaps;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.function.Function;

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

        // TODO: turn this into a FadFactory class
        final Function<FadManager, Fad> fadFactory = (owner) -> {
            final BiomassLocalBiology fadBiology =
                new BiomassLocalBiology(1.0, fishState.getBiology().getSize(), fishState.random);
            return new Fad(owner, fadBiology, 0.01);
        };

        final CurrentMaps currentMaps = makeCurrentsMaps(nauticalMap, currentsVectorFilePath);

        return new FadMap(nauticalMap, currentMaps, fishState.getBiology(), fadFactory);
    }

}
