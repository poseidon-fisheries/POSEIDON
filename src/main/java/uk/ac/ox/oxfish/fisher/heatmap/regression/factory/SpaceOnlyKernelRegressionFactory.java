package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.SpaceOnlyKernelRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/4/16.
 */
public class SpaceOnlyKernelRegressionFactory implements AlgorithmFactory<SpaceOnlyKernelRegression>{


    private DoubleParameter spaceBandwidth= new FixedDoubleParameter(5d);


    private DoubleParameter maximumNumberOfObservations= new FixedDoubleParameter(100d);



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SpaceOnlyKernelRegression apply(FishState state) {
        return new SpaceOnlyKernelRegression(
                maximumNumberOfObservations.apply(state.getRandom()).intValue(),
                spaceBandwidth.apply(state.getRandom())
        );
    }

    public DoubleParameter getSpaceBandwidth() {
        return spaceBandwidth;
    }

    public void setSpaceBandwidth(DoubleParameter spaceBandwidth) {
        this.spaceBandwidth = spaceBandwidth;
    }

    public DoubleParameter getMaximumNumberOfObservations() {
        return maximumNumberOfObservations;
    }

    public void setMaximumNumberOfObservations(DoubleParameter maximumNumberOfObservations) {
        this.maximumNumberOfObservations = maximumNumberOfObservations;
    }
}
