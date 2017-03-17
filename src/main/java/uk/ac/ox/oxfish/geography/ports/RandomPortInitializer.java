package uk.ac.ox.oxfish.geography.ports;

import ec.util.MersenneTwisterFast;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.*;
import java.util.function.Function;

/**
 * Created by carrknight on 1/21/17.
 */
public class RandomPortInitializer implements PortInitializer {


    private final int ports;


    public RandomPortInitializer(int ports) {
        this.ports = ports;
    }

    /**
     * add random ports to the map
     * @param map
     */
    public static List<Port> addRandomPortsToMap(NauticalMap map,int ports,
                                           Function<SeaTile,MarketMap> marketFactory,
                                           MersenneTwisterFast random){

        List<Port> toReturn = new LinkedList<>();

        /***
         *        _      _    _   ___         _
         *       /_\  __| |__| | | _ \___ _ _| |_ ___
         *      / _ \/ _` / _` | |  _/ _ \ '_|  _(_-<
         *     /_/ \_\__,_\__,_| |_| \___/_|  \__/__/
         *
         */
        ObjectGrid2D baseGrid = (ObjectGrid2D) map.getRasterBathymetry().getGrid();
        int width = baseGrid.getWidth();
        int height = baseGrid.getHeight();

        ArrayList<SeaTile> candidateTiles = new ArrayList<>();
        for(int x=0; x<width; x++)
            for(int y=0; y<height; y++)
            {

                SeaTile possible = (SeaTile) baseGrid.get(x, y);
                if(possible.getAltitude() <= 0) //sea tiles aren't welcome!
                    continue;
                int neighboringSeaTiles = 0;
                Bag neighbors = new Bag();
                baseGrid.getMooreNeighbors(x, y, 1, Grid2D.BOUNDED, false, neighbors, null, null);
                for(Object neighbor : neighbors)
                    if(((SeaTile)neighbor).getAltitude() < 0 )
                        neighboringSeaTiles++;

                if(neighboringSeaTiles >=1)
                    candidateTiles.add(possible);

            }
        //get all candidates (land tiles with at least 4 sea tiles next to them)

        Collections.shuffle(candidateTiles, new Random(random.nextLong()));
        for(int i=0; i<ports; i++) {
            Port port = new Port("Port " + i, candidateTiles.get(i), marketFactory.apply(candidateTiles.get(i)), 0);
            map.addPort(port);
            toReturn.add(port);
        }

        return toReturn;
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
     * @return the list of ports that have been built and added to the map. It can be ignored.
     */
    @Override
    public List<Port> buildPorts(
            NauticalMap map, MersenneTwisterFast mapmakerRandom, Function<SeaTile, MarketMap> marketFactory,
            FishState model) {
        return addRandomPortsToMap(map,getPorts(),marketFactory,mapmakerRandom);
    }

    /**
     * Getter for property 'ports'.
     *
     * @return Value for property 'ports'.
     */
    public int getPorts() {
        return ports;
    }
}
