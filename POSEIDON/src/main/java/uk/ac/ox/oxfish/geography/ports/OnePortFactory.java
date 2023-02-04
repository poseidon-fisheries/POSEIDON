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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/21/17.
 */
public class OnePortFactory implements AlgorithmFactory<OnePortInitializer> {


    /**
     * the X position of the port on the grid.
     */
    private DoubleParameter portPositionX = new FixedDoubleParameter(40);
    /**
     * the X position of the port on the grid.
     */
    private DoubleParameter portPositionY = new FixedDoubleParameter(25);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public OnePortInitializer apply(FishState fishState) {

        return new OnePortInitializer(portPositionX.apply(fishState.getRandom()).intValue(),
                                      portPositionY.apply(fishState.getRandom()).intValue());
    }


    /**
     * Getter for property 'portPositionX'.
     *
     * @return Value for property 'portPositionX'.
     */
    public DoubleParameter getPortPositionX() {
        return portPositionX;
    }

    /**
     * Setter for property 'portPositionX'.
     *
     * @param portPositionX Value to set for property 'portPositionX'.
     */
    public void setPortPositionX(DoubleParameter portPositionX) {
        this.portPositionX = portPositionX;
    }

    /**
     * Getter for property 'portPositionY'.
     *
     * @return Value for property 'portPositionY'.
     */
    public DoubleParameter getPortPositionY() {
        return portPositionY;
    }

    /**
     * Setter for property 'portPositionY'.
     *
     * @param portPositionY Value to set for property 'portPositionY'.
     */
    public void setPortPositionY(DoubleParameter portPositionY) {
        this.portPositionY = portPositionY;
    }
}
