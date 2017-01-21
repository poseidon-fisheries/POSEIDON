package uk.ac.ox.oxfish.geography.ports;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/21/17.
 */
public class TwoPortsFactory implements AlgorithmFactory<TwoPortsInitializer>
{

    /**
     * the X position of the port on the grid.
     */
    private DoubleParameter port1PositionX = new FixedDoubleParameter(40);
    /**
     * the X position of the port on the grid.
     */
    private DoubleParameter port1PositionY = new FixedDoubleParameter(0);



    /**
     * the X position of the port on the grid.
     */
    private  DoubleParameter port2PositionX = new FixedDoubleParameter(40);
    /**
     * the X position of the port on the grid.
     */
    private DoubleParameter port2PositionY = new FixedDoubleParameter(49);

    private String namePort1 = "Washington";

    private String namePort2 = "California";


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public TwoPortsInitializer apply(FishState fishState) {
        return new TwoPortsInitializer(
                port1PositionX.apply(fishState.getRandom()).intValue(),
                port1PositionY.apply(fishState.getRandom()).intValue(),
                port2PositionX.apply(fishState.getRandom()).intValue(),
                port2PositionY.apply(fishState.getRandom()).intValue(),
                namePort1,
                namePort2
        );
    }

    /**
     * Getter for property 'port1PositionX'.
     *
     * @return Value for property 'port1PositionX'.
     */
    public DoubleParameter getPort1PositionX() {
        return port1PositionX;
    }

    /**
     * Setter for property 'port1PositionX'.
     *
     * @param port1PositionX Value to set for property 'port1PositionX'.
     */
    public void setPort1PositionX(DoubleParameter port1PositionX) {
        this.port1PositionX = port1PositionX;
    }

    /**
     * Getter for property 'port1PositionY'.
     *
     * @return Value for property 'port1PositionY'.
     */
    public DoubleParameter getPort1PositionY() {
        return port1PositionY;
    }

    /**
     * Setter for property 'port1PositionY'.
     *
     * @param port1PositionY Value to set for property 'port1PositionY'.
     */
    public void setPort1PositionY(DoubleParameter port1PositionY) {
        this.port1PositionY = port1PositionY;
    }

    /**
     * Getter for property 'port2PositionX'.
     *
     * @return Value for property 'port2PositionX'.
     */
    public DoubleParameter getPort2PositionX() {
        return port2PositionX;
    }

    /**
     * Setter for property 'port2PositionX'.
     *
     * @param port2PositionX Value to set for property 'port2PositionX'.
     */
    public void setPort2PositionX(DoubleParameter port2PositionX) {
        this.port2PositionX = port2PositionX;
    }

    /**
     * Getter for property 'port2PositionY'.
     *
     * @return Value for property 'port2PositionY'.
     */
    public DoubleParameter getPort2PositionY() {
        return port2PositionY;
    }

    /**
     * Setter for property 'port2PositionY'.
     *
     * @param port2PositionY Value to set for property 'port2PositionY'.
     */
    public void setPort2PositionY(DoubleParameter port2PositionY) {
        this.port2PositionY = port2PositionY;
    }

    /**
     * Getter for property 'namePort1'.
     *
     * @return Value for property 'namePort1'.
     */
    public String getNamePort1() {
        return namePort1;
    }

    /**
     * Setter for property 'namePort1'.
     *
     * @param namePort1 Value to set for property 'namePort1'.
     */
    public void setNamePort1(String namePort1) {
        this.namePort1 = namePort1;
    }

    /**
     * Getter for property 'namePort2'.
     *
     * @return Value for property 'namePort2'.
     */
    public String getNamePort2() {
        return namePort2;
    }

    /**
     * Setter for property 'namePort2'.
     *
     * @param namePort2 Value to set for property 'namePort2'.
     */
    public void setNamePort2(String namePort2) {
        this.namePort2 = namePort2;
    }
}
