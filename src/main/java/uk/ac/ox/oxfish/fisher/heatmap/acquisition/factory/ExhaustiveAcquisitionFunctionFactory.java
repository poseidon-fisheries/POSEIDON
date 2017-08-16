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
    private boolean ignoreProtectedAreas = true;
    private boolean ignoreWastelands = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExhaustiveAcquisitionFunction apply(FishState state) {
        return new ExhaustiveAcquisitionFunction(proportionSearched.apply(state.getRandom()), ignoreProtectedAreas,
                                                 ignoreWastelands);
    }


    public DoubleParameter getProportionSearched() {
        return proportionSearched;
    }

    public void setProportionSearched(DoubleParameter proportionSearched) {
        this.proportionSearched = proportionSearched;
    }

    /**
     * Getter for property 'ignoreProtectedAreas'.
     *
     * @return Value for property 'ignoreProtectedAreas'.
     */
    public boolean isIgnoreProtectedAreas() {
        return ignoreProtectedAreas;
    }

    /**
     * Setter for property 'ignoreProtectedAreas'.
     *
     * @param ignoreProtectedAreas Value to set for property 'ignoreProtectedAreas'.
     */
    public void setIgnoreProtectedAreas(boolean ignoreProtectedAreas) {
        this.ignoreProtectedAreas = ignoreProtectedAreas;
    }

    /**
     * Getter for property 'ignoreWastelands'.
     *
     * @return Value for property 'ignoreWastelands'.
     */
    public boolean isIgnoreWastelands() {
        return ignoreWastelands;
    }

    /**
     * Setter for property 'ignoreWastelands'.
     *
     * @param ignoreWastelands Value to set for property 'ignoreWastelands'.
     */
    public void setIgnoreWastelands(boolean ignoreWastelands) {
        this.ignoreWastelands = ignoreWastelands;
    }
}
