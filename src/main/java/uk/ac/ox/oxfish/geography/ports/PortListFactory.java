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

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;

/**
 * List of ports (actually a map!)
 * Created by carrknight on 3/13/17.
 */
public class PortListFactory implements AlgorithmFactory<PortListInitializer> {

    private LinkedHashMap<String,Coordinate> ports = new LinkedHashMap<>();
    {
        ports.put("Top Port", new Coordinate(40,0));
        ports.put("Middle Port", new Coordinate(40,24));
        ports.put("Bottom Port", new Coordinate(40,49));
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PortListInitializer apply(FishState state) {
        return new PortListInitializer(ports);
    }

    /**
     * Getter for property 'ports'.
     *
     * @return Value for property 'ports'.
     */
    public LinkedHashMap<String, Coordinate> getPorts() {
        return ports;
    }

    /**
     * Setter for property 'ports'.
     *
     * @param ports Value to set for property 'ports'.
     */
    public void setPorts(LinkedHashMap<String, Coordinate> ports) {
        this.ports = ports;
    }
}
