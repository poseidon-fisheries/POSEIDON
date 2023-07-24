package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class ReliableFishValueCalculatorFactory implements AlgorithmFactory<FishValueCalculator> {
    @Override
    public FishValueCalculator apply(final FishState fishState) {
        return new ReliableFishValueCalculator(fishState.getBiology());
    }
}
