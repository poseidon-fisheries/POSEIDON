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

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBoxcarPulseRecruitmentFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.YamlConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * List of ports (actually a map!)
 * Created by carrknight on 3/13/17.
 */
public class PortListFactory implements AlgorithmFactory<PortListInitializer> {

    private LinkedHashMap<String, String> ports = new LinkedHashMap<>();
    private boolean usingGridCoordinates = true;

    {
        ports.put("Top Port", "x:40,y:0");
        ports.put("Middle Port", "x:40,y:24");
        ports.put("Bottom Port", "x:40,y:49");
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PortListInitializer apply(FishState state) {

        ports = SingleSpeciesBoxcarPulseRecruitmentFactory.forceThroughYaml(ports, String.class);
        LinkedHashMap<String, Coordinate> coordinatedPorts = new LinkedHashMap<>();

        for (Map.Entry<String, String> stringPort : ports.entrySet()) {

            coordinatedPorts.put(
                stringPort.getKey(),
                YamlConstructor.convertToCoordinate(
                    stringPort.getValue()
                )
            );

        }


        return new PortListInitializer(coordinatedPorts, usingGridCoordinates);
    }

    /**
     * Getter for property 'ports'.
     *
     * @return Value for property 'ports'.
     */
    public LinkedHashMap<String, String> getPorts() {
        return ports;
    }

    /**
     * Setter for property 'ports'.
     *
     * @param ports Value to set for property 'ports'.
     */
    public void setPorts(LinkedHashMap<String, String> ports) {
        this.ports = ports;
    }


    public boolean isUsingGridCoordinates() {
        return usingGridCoordinates;
    }

    public void setUsingGridCoordinates(boolean usingGridCoordinates) {
        this.usingGridCoordinates = usingGridCoordinates;
    }
}
