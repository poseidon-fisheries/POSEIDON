package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.TimeAndSpaceKernelRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/4/16.
 */
public class TimeAndSpaceKernelRegressionFactory implements AlgorithmFactory<TimeAndSpaceKernelRegression> {


    private DoubleParameter timeBandwidth = new FixedDoubleParameter(1000d);


    private DoubleParameter spaceBandwidth = new FixedDoubleParameter(5d);


    private DoubleParameter maximumNumberOfObservations= new FixedDoubleParameter(100d);



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public TimeAndSpaceKernelRegression apply(FishState state) {
        return new TimeAndSpaceKernelRegression(
                timeBandwidth.apply(state.getRandom()),
                spaceBandwidth.apply(state.getRandom()),
                maximumNumberOfObservations.apply(state.getRandom()).intValue()
                );
    }

    public DoubleParameter getTimeBandwidth() {
        return timeBandwidth;
    }

    public void setTimeBandwidth(DoubleParameter timeBandwidth) {
        this.timeBandwidth = timeBandwidth;
    }

    public DoubleParameter getMaximumNumberOfObservations() {
        return maximumNumberOfObservations;
    }

    public void setMaximumNumberOfObservations(DoubleParameter maximumNumberOfObservations) {
        this.maximumNumberOfObservations = maximumNumberOfObservations;
    }

    public DoubleParameter getSpaceBandwidth() {
        return spaceBandwidth;
    }

    public void setSpaceBandwidth(DoubleParameter spaceBandwidth) {
        this.spaceBandwidth = spaceBandwidth;
    }
}