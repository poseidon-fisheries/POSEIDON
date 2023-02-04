package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.NoFishing;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class NoFishingFactory implements AlgorithmFactory<NoFishing> {
    private static NoFishing singleton = new NoFishing();
    @Override public NoFishing apply(FishState fishState) { return singleton; }
}
