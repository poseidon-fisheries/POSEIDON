/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.geography.ports;

import ec.util.MersenneTwisterFast;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;

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
     * @param maker
     * @param model
     */
    public static List<Port> addRandomPortsToMap(
            NauticalMap map, int ports,
            Function<SeaTile, MarketMap> marketFactory,
            MersenneTwisterFast random, GasPriceMaker maker, FishState model){

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
                if(possible.isWater()) //sea tiles aren't welcome!
                    continue;
                int neighboringSeaTiles = 0;
                Bag neighbors = new Bag();
                baseGrid.getMooreNeighbors(x, y, 1, Grid2D.BOUNDED, false, neighbors, null, null);
                for(Object neighbor : neighbors)
                    if(((SeaTile)neighbor).isWater())
                        neighboringSeaTiles++;

                if(neighboringSeaTiles >=1)
                    candidateTiles.add(possible);

            }
        //get all candidates (land tiles with at least 4 sea tiles next to them)

        Collections.shuffle(candidateTiles, new Random(random.nextLong()));
        for(int i=0; i<ports; i++) {
            Port port = new Port("Port " + i, candidateTiles.get(i), marketFactory.apply(candidateTiles.get(i)),
                                 maker.supplyInitialPrice(candidateTiles.get(i),"Port " + i));
            maker.start(port,model);
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
     * @param gasPriceMaker
     * @return the list of ports that have been built and added to the map. It can be ignored.
     */
    @Override
    public List<Port> buildPorts(
            NauticalMap map, MersenneTwisterFast mapmakerRandom, Function<SeaTile, MarketMap> marketFactory,
            FishState model, GasPriceMaker gasPriceMaker) {
        return addRandomPortsToMap(map, getPorts(), marketFactory, mapmakerRandom, gasPriceMaker,model);
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
