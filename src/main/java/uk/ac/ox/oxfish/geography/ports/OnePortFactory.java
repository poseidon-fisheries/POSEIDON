package uk.ac.ox.oxfish.geography.ports;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/21/17.
 */
public class OnePortFactory implements AlgorithmFactory<OnePortInitializer> {


    /**
     * the X position of the port on the grid.
     */
    private DoubleParameter portPositionX = new FixedDoubleParameter(40);
    /**
     * the X position of the port on the grid.
     */
    private DoubleParameter portPositionY = new FixedDoubleParameter(25);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public OnePortInitializer apply(FishState fishState) {

        return new OnePortInitializer(portPositionX.apply(fishState.getRandom()).intValue(),
                                      portPositionY.apply(fishState.getRandom()).intValue());
    }


    /**
     * Getter for property 'portPositionX'.
     *
     * @return Value for property 'portPositionX'.
     */
    public DoubleParameter getPortPositionX() {
        return portPositionX;
    }

    /**
     * Setter for property 'portPositionX'.
     *
     * @param portPositionX Value to set for property 'portPositionX'.
     */
    public void setPortPositionX(DoubleParameter portPositionX) {
        this.portPositionX = portPositionX;
    }

    /**
     * Getter for property 'portPositionY'.
     *
     * @return Value for property 'portPositionY'.
     */
    public DoubleParameter getPortPositionY() {
        return portPositionY;
    }

    /**
     * Setter for property 'portPositionY'.
     *
     * @param portPositionY Value to set for property 'portPositionY'.
     */
    public void setPortPositionY(DoubleParameter portPositionY) {
        this.portPositionY = portPositionY;
    }
}
