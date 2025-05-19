/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.geography.ports;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;

import java.util.List;
import java.util.function.Function;

/**
 * Creates a single port at specified location
 * Created by carrknight on 1/21/17.
 */
public class OnePortInitializer implements PortInitializer {


    /**
     * the X position of the port on the grid.
     */
    private final int portPositionX;
    /**
     * the X position of the port on the grid.
     */
    private final int portPositionY;


    public OnePortInitializer(int portPositionX, int portPositionY) {
        this.portPositionX = portPositionX;
        this.portPositionY = portPositionY;
    }

    /**
     * Creates single port in tile specified
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
        FishState model, GasPriceMaker gasPriceMaker
    ) {
        SeaTile here = map.getSeaTile(portPositionX, portPositionY);
        Port port = new Port("Port 0", here,
            marketFactory.apply(here),
            gasPriceMaker.supplyInitialPrice(here, "Port 0")
        );
        gasPriceMaker.start(port, model);

        map.addPort(port);

        return Lists.newArrayList(port);
    }
}
