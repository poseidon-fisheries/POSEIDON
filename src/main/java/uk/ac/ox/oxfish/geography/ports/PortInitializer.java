package uk.ac.ox.oxfish.geography.ports;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.List;
import java.util.function.Function;

/**
 * Called by the scenario to build ports and add them to the map
 * Created by carrknight on 1/21/17.
 */
public interface PortInitializer {


    /**
     * Create and add ports to map, return them as a list.
     * Supposedly this is called during the early scenario setup. The map is built, the biology is built
     * and the marketmap can be built.
     * @param map the map to place ports in
     * @param mapmakerRandom the randomizer
     * @param marketFactory a function that returns the market associated with a location. We might refactor this at some point*
     * @param model
     * @return the list of ports that have been built and added to the map. It can be ignored.
     */
    public List<Port> buildPorts(
            NauticalMap map,
            MersenneTwisterFast mapmakerRandom,
            Function<SeaTile, MarketMap> marketFactory, FishState model);
}
