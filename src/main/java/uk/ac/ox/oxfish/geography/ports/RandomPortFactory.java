package uk.ac.ox.oxfish.geography.ports;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/21/17.
 */
public class RandomPortFactory implements AlgorithmFactory<RandomPortInitializer> {


    private DoubleParameter numberOfPorts = new FixedDoubleParameter(1);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public RandomPortInitializer apply(FishState fishState) {
        return new RandomPortInitializer(numberOfPorts.apply(fishState.getRandom()).intValue());
    }

    /**
     * Getter for property 'ports'.
     *
     * @return Value for property 'ports'.
     */
    public DoubleParameter getNumberOfPorts() {
        return numberOfPorts;
    }

    /**
     * Setter for property 'ports'.
     *
     * @param numberOfPorts Value to set for property 'ports'.
     */
    public void setNumberOfPorts(DoubleParameter numberOfPorts) {
        this.numberOfPorts = numberOfPorts;
    }
}
