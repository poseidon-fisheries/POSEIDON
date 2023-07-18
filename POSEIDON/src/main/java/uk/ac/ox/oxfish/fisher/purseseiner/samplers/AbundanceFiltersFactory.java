package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public interface AbundanceFiltersFactory extends AlgorithmFactory<AbundanceFilters> {
    @Override
    AbundanceFilters apply(
        FishState fishState
    );
}
