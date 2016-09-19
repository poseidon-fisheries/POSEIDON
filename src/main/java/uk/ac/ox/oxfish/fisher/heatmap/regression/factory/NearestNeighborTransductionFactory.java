package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.CartesianRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.TimeExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborTransduction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;


/**
 * Created by carrknight on 7/4/16.
 */
public class NearestNeighborTransductionFactory implements AlgorithmFactory<NearestNeighborTransduction> {


    private DoubleParameter timeBandwidth = new FixedDoubleParameter(1000d);


    private DoubleParameter spaceBandwidth = new FixedDoubleParameter(5d);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public NearestNeighborTransduction apply(FishState state) {
        return new NearestNeighborTransduction(
                state.getMap(),
                new ObservationExtractor[]{
                        new GridYExtractor(),
                        new GridXExtractor(),
                        new TimeExtractor()
                },
                new double[]{
                        spaceBandwidth.apply(state.getRandom()),
                        spaceBandwidth.apply(state.getRandom()),
                        timeBandwidth.apply(state.getRandom())
                },
                new CartesianRegressionDistance(0) //gets changed by the regression

        );
    }


    /**
     * Getter for property 'timeBandwidth'.
     *
     * @return Value for property 'timeBandwidth'.
     */
    public DoubleParameter getTimeBandwidth() {
        return timeBandwidth;
    }

    /**
     * Setter for property 'timeBandwidth'.
     *
     * @param timeBandwidth Value to set for property 'timeBandwidth'.
     */
    public void setTimeBandwidth(DoubleParameter timeBandwidth) {
        this.timeBandwidth = timeBandwidth;
    }

    /**
     * Getter for property 'spaceBandwidth'.
     *
     * @return Value for property 'spaceBandwidth'.
     */
    public DoubleParameter getSpaceBandwidth() {
        return spaceBandwidth;
    }

    /**
     * Setter for property 'spaceBandwidth'.
     *
     * @param spaceBandwidth Value to set for property 'spaceBandwidth'.
     */
    public void setSpaceBandwidth(DoubleParameter spaceBandwidth) {
        this.spaceBandwidth = spaceBandwidth;
    }


}
