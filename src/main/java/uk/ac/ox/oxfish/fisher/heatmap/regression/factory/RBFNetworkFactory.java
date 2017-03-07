package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.basis.RBFNetworkRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 3/7/17.
 */
public class RBFNetworkFactory  implements AlgorithmFactory<RBFNetworkRegression>{


    private final DoubleParameter learningRate = new FixedDoubleParameter(100);

    private final DoubleParameter initialWeight = new FixedDoubleParameter(1000);


    private final DoubleParameter order = new FixedDoubleParameter(5);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RBFNetworkRegression apply(FishState state) {
        return new RBFNetworkRegression(

                new ObservationExtractor[]{
                        new GridXExtractor(),
                        new GridYExtractor()
                },order.apply(state.getRandom()).intValue(),
                new double[]{0,0},
                new double[]{state.getMap().getWidth(),state.getMap().getHeight()},
                learningRate.apply(state.getRandom()),
                initialWeight.apply(state.getRandom())
        );
    }


    /**
     * Getter for property 'learningRate'.
     *
     * @return Value for property 'learningRate'.
     */
    public DoubleParameter getLearningRate() {
        return learningRate;
    }

    /**
     * Getter for property 'initialWeight'.
     *
     * @return Value for property 'initialWeight'.
     */
    public DoubleParameter getInitialWeight() {
        return initialWeight;
    }

    /**
     * Getter for property 'order'.
     *
     * @return Value for property 'order'.
     */
    public DoubleParameter getOrder() {
        return order;
    }
}
