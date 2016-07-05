package uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory;

import uk.ac.ox.oxfish.fisher.heatmap.acquisition.ExhaustiveAcquisitionFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 7/5/16.
 */
public class ExhaustiveAcquisitionFunctionFactory implements AlgorithmFactory<ExhaustiveAcquisitionFunction> {



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExhaustiveAcquisitionFunction apply(FishState state) {
        return new ExhaustiveAcquisitionFunction();
    }
}
