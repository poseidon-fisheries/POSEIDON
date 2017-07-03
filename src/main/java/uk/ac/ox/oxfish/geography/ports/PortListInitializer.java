package uk.ac.ox.oxfish.geography.ports;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by carrknight on 3/13/17.
 */
public class PortListInitializer implements PortInitializer {


    private final LinkedHashMap<String,Coordinate> ports;

    public PortListInitializer(LinkedHashMap<String, Coordinate> ports) {
        this.ports = ports;
        Preconditions.checkArgument(ports.size()>0);
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
     * @param gasPrice
     * @return the list of ports that have been built and added to the map. It can be ignored.
     */
    @Override
    public List<Port> buildPorts(
            NauticalMap map, MersenneTwisterFast mapmakerRandom, Function<SeaTile, MarketMap> marketFactory,
            FishState model, double gasPrice) {
        List<Port> toReturn = new ArrayList<>(ports.size());
        for(Map.Entry<String,Coordinate> entry : ports.entrySet()) {
            SeaTile location = map.getSeaTile((int) entry.getValue().x,
                                              (int) entry.getValue().y);

            Port newPort = new Port(entry.getKey(),
                              location,
                              marketFactory.apply(location),
                              //ports start with price = 0 because I assume the scenario will have its own rules for gas price

                              0
            );
            toReturn.add(newPort);
            map.addPort(newPort);
        }




        return toReturn;
    }
}
