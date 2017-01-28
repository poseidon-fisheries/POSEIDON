package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.KnifeEdgePerTripObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/28/17.
 */
public class KnifeEdgePerTripFactory implements AlgorithmFactory<KnifeEdgePerTripObjective> {


    /**
     * opportunity costs
     */
    private boolean opportunityCosts = true;

    /**
     * minimum amount of $/hr to make utility positive
     */
    private DoubleParameter threshold = new FixedDoubleParameter(10d);

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public KnifeEdgePerTripObjective apply(FishState fishState) {
        return new KnifeEdgePerTripObjective(opportunityCosts,
                                             threshold.apply(fishState.getRandom())
        );
    }


    /**
     * Getter for property 'opportunityCosts'.
     *
     * @return Value for property 'opportunityCosts'.
     */
    public boolean isOpportunityCosts() {
        return opportunityCosts;
    }

    /**
     * Setter for property 'opportunityCosts'.
     *
     * @param opportunityCosts Value to set for property 'opportunityCosts'.
     */
    public void setOpportunityCosts(boolean opportunityCosts) {
        this.opportunityCosts = opportunityCosts;
    }

    /**
     * Getter for property 'threshold'.
     *
     * @return Value for property 'threshold'.
     */
    public DoubleParameter getThreshold() {
        return threshold;
    }

    /**
     * Setter for property 'threshold'.
     *
     * @param threshold Value to set for property 'threshold'.
     */
    public void setThreshold(DoubleParameter threshold) {
        this.threshold = threshold;
    }
}
