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

package uk.ac.ox.oxfish.geography.habitat.rectangles;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * A factory that creates the rocky rectangle habitat initializers.
 * Created by carrknight on 9/29/15.
 */
public class RockyRectanglesHabitatFactory
        implements AlgorithmFactory<RockyRectanglesHabitatInitializer>
{


    private DoubleParameter rockyHeight = new UniformDoubleParameter(25,30);
    private DoubleParameter rockyWidth = new UniformDoubleParameter(20,25);

    private DoubleParameter numberOfRectangles = new FixedDoubleParameter(1);


    public RockyRectanglesHabitatFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RockyRectanglesHabitatInitializer apply(FishState state) {
        return new RockyRectanglesHabitatInitializer(rockyHeight,rockyWidth,
                                              numberOfRectangles.apply(state.getRandom()).intValue());
    }

    public DoubleParameter getRockyHeight() {
        return rockyHeight;
    }

    public void setRockyHeight(DoubleParameter rockyHeight) {
        this.rockyHeight = rockyHeight;
    }

    public DoubleParameter getRockyWidth() {
        return rockyWidth;
    }

    public void setRockyWidth(DoubleParameter rockyWidth) {
        this.rockyWidth = rockyWidth;
    }

    public DoubleParameter getNumberOfRectangles() {
        return numberOfRectangles;
    }

    public void setNumberOfRectangles(DoubleParameter numberOfRectangles) {
        this.numberOfRectangles = numberOfRectangles;
    }
}
