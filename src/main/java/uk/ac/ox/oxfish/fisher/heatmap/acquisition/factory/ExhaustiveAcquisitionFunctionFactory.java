package uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory;

import uk.ac.ox.oxfish.fisher.heatmap.acquisition.ExhaustiveAcquisitionFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/5/16.
 */
public class ExhaustiveAcquisitionFunctionFactory implements AlgorithmFactory<ExhaustiveAcquisitionFunction> {



    private DoubleParameter proportionSearched = new FixedDoubleParameter(1d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExhaustiveAcquisitionFunction apply(FishState state) {
        return new ExhaustiveAcquisitionFunction(proportionSearched.apply(state.getRandom()));
    }


    public DoubleParameter getProportionSearched() {
        return proportionSearched;
    }

    public void setProportionSearched(DoubleParameter proportionSearched) {
        this.proportionSearched = proportionSearched;
    }
}
