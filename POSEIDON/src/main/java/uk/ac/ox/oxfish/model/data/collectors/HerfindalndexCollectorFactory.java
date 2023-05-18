package uk.ac.ox.oxfish.model.data.collectors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class HerfindalndexCollectorFactory implements
    AlgorithmFactory<HerfindalIndexCollector> {

    @Override
    public HerfindalIndexCollector apply(FishState fishState) {
        return new HerfindalIndexCollector();
    }
}
