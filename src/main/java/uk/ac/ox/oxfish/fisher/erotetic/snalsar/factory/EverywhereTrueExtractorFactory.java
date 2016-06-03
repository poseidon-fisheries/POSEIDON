package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;


import uk.ac.ox.oxfish.fisher.erotetic.snalsar.EverywhereTrueExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class EverywhereTrueExtractorFactory implements AlgorithmFactory<EverywhereTrueExtractor> {

    @Override
    public EverywhereTrueExtractor apply(FishState state) {
        return new EverywhereTrueExtractor();
    }
}
