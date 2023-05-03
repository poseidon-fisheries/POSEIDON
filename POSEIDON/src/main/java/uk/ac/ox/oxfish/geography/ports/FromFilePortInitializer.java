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

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.utility.parameters.PortReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Created by carrknight on 7/2/17.
 */
public class FromFilePortInitializer implements PortInitializer {

    /**
     * the file reading the ports
     */
    private final PortReader reader = new PortReader();

    private Path filePath;

    private LinkedHashMap<Port, Integer> portMap;


    public FromFilePortInitializer(Path filePath) {
        this.filePath = filePath;
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
        try {
            portMap = reader.readFile(
                    filePath,map,marketFactory,
                    gasPriceMaker,
                    model

            );
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to read port file!");
        }
        for(Port port : portMap.keySet())
            map.addPort(port);
        return Lists.newArrayList(portMap.keySet());
    }


    /**
     * returns a list of fishers per port in form of a map tags--->port
     * @param port port
     * @return # of fishers to instantiate per tags
     */
    public Integer getFishersPerPort(Port port){

        return portMap.get(port);

    }

    /**
     * Getter for property 'filePath'.
     *
     * @return Value for property 'filePath'.
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * Setter for property 'filePath'.
     *
     * @param filePath Value to set for property 'filePath'.
     */
    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }
}
