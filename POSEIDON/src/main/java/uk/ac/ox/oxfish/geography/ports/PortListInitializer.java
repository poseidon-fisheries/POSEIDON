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

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.utility.parameters.PortReader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by carrknight on 3/13/17.
 */
public class PortListInitializer implements PortInitializer {


    /**
     * if this is set to false, we assume the coordinates provided are geographical coordinates;
     * if this is set to true, we assume the coordinates provided are grid coordinates
     */
    private final boolean usingGridCoordinates;
    private LinkedHashMap<String, Coordinate> ports;

    public PortListInitializer(LinkedHashMap<String, Coordinate> ports, boolean usingGridCoordinates) {
//        this.ports = forceThroughYaml(ports,
//                Coordinate.class);;
        this.ports = ports;
        this.usingGridCoordinates = usingGridCoordinates;
        Preconditions.checkArgument(ports.size() > 0);
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
        FishState model, GasPriceMaker gasPriceMaker
    ) {
        List<Port> toReturn = new ArrayList<>(ports.size());
        for (Map.Entry<String, Coordinate> entry : ports.entrySet()) {


            SeaTile location;
            if (usingGridCoordinates)
                location = map.getSeaTile(
                    (int) entry.getValue().x,
                    (int) entry.getValue().y
                );
            else {
                location = PortReader.correctLocation(
                    map.getSeaTile(
                        entry.getValue()
                    ),
                    map,
                    entry.getKey()
                );
            }

            Port newPort = new Port(
                entry.getKey(),
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

    public LinkedHashMap<String, Coordinate> getPorts() {
        return ports;
    }

    public void setPorts(LinkedHashMap<String, Coordinate> ports) {
        this.ports = ports;
//        this.ports = forceThroughYaml(ports,
//                Coordinate.class);
    }

    public boolean isUsingGridCoordinates() {
        return usingGridCoordinates;
    }
}
