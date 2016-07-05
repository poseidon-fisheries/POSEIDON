package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.TimeOnlyKernelRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/4/16.
 */
public class TimeOnlyKernelRegressionFactory implements AlgorithmFactory<TimeOnlyKernelRegression> {


    private DoubleParameter timeBandwidth = new FixedDoubleParameter(1000d);


    private DoubleParameter maximumNumberOfObservations= new FixedDoubleParameter(100d);



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public TimeOnlyKernelRegression apply(FishState state) {
        return new TimeOnlyKernelRegression(
                maximumNumberOfObservations.apply(state.getRandom()).intValue(),
                timeBandwidth.apply(state.getRandom())
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
}
