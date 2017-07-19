package uk.ac.ox.oxfish.geography.ports;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by carrknight on 1/21/17.
 */
public class TwoPortsInitializer implements PortInitializer {



    /**
     * the X position of the port on the grid.
     */
    private  final int port1PositionX;
    /**
     * the X position of the port on the grid.
     */
    private final int port1PositionY;



    /**
     * the X position of the port on the grid.
     */
    private  final int port2PositionX;
    /**
     * the X position of the port on the grid.
     */
    private final int port2PositionY;

    private final String namePort1;

    private final String namePort2;

    public TwoPortsInitializer(
            int port1PositionX, int port1PositionY, int port2PositionX, int port2PositionY, String namePort1,
            String namePort2) {
        Preconditions.checkArgument(port1PositionX!=port2PositionX || port1PositionY != port2PositionY);

        Preconditions.checkArgument(!Objects.equals(namePort1, namePort2));

        this.port1PositionX = port1PositionX;
        this.port1PositionY = port1PositionY;
        this.port2PositionX = port2PositionX;
        this.port2PositionY = port2PositionY;
        this.namePort1 = namePort1;
        this.namePort2 = namePort2;
    }

    /**
     * Create and add ports to map, return them as a list.
     * Supposedly this is called during the early scenario setup. The map is built, the biology is built
     * and the marketmap can be built.
     *
     * @param map            the map to place ports in
     * @param mapmakerRandom the randomizer
     * @param marketFactory  a function that returns the market associated with a location. We might refactor this at some point*
     * @param model
     * @param gasPriceMaker
     * @return the list of ports that have been built and added to the map. It can be ignored.
     */
    @Override
    public List<Port> buildPorts(
            NauticalMap map,
            MersenneTwisterFast mapmakerRandom,
            Function<SeaTile, MarketMap> marketFactory, FishState model,
            GasPriceMaker gasPriceMaker) {

        //ports start with price = 0 because I assume the scenario will have its own rules for gas price

        SeaTile first = map.getSeaTile(port1PositionX, port1PositionY);
        Port port1 = new Port(namePort1, first,
                             marketFactory.apply(first),
                              gasPriceMaker.supplyInitialPrice(first,namePort1));
        map.addPort(port1);

        SeaTile second = map.getSeaTile(port2PositionX,port2PositionY);
        Port port2 = new Port(namePort2, second,
                              marketFactory.apply(second),
                              gasPriceMaker.supplyInitialPrice(second,namePort2));
        map.addPort(port2);

        gasPriceMaker.start(port1,model);
        gasPriceMaker.start(port2,model);

        return Lists.newArrayList(port1,port2);
    }


    /**
     * Getter for property 'port1PositionX'.
     *
     * @return Value for property 'port1PositionX'.
     */
    public int getPort1PositionX() {
        return port1PositionX;
    }

    /**
     * Getter for property 'port1PositionY'.
     *
     * @return Value for property 'port1PositionY'.
     */
    public int getPort1PositionY() {
        return port1PositionY;
    }

    /**
     * Getter for property 'port2PositionX'.
     *
     * @return Value for property 'port2PositionX'.
     */
    public int getPort2PositionX() {
        return port2PositionX;
    }

    /**
     * Getter for property 'port2PositionY'.
     *
     * @return Value for property 'port2PositionY'.
     */
    public int getPort2PositionY() {
        return port2PositionY;
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
     * Getter for property 'namePort2'.
     *
     * @return Value for property 'namePort2'.
     */
    public String getNamePort2() {
        return namePort2;
    }
}
