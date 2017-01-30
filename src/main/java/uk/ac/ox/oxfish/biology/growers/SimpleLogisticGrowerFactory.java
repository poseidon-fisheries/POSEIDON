package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * Created by carrknight on 1/31/17.
 */
public class SimpleLogisticGrowerFactory implements AlgorithmFactory<SimpleLogisticGrowerInitializer> {

    private DoubleParameter steepness = new FixedDoubleParameter(0.7);


    public SimpleLogisticGrowerFactory() {
    }


    public SimpleLogisticGrowerFactory(double steepness) {
        this.steepness = new FixedDoubleParameter(steepness);
    }


    public SimpleLogisticGrowerFactory(double low,double high) {
        this.steepness =  new UniformDoubleParameter(low, high);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SimpleLogisticGrowerInitializer apply(FishState state) {
        return new SimpleLogisticGrowerInitializer(steepness.makeCopy());
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public DoubleParameter getSteepness() {
        return steepness;
    }

    /**
     * Setter for property 'steepness'.
     *
     * @param steepness Value to set for property 'steepness'.
     */
    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }
}
