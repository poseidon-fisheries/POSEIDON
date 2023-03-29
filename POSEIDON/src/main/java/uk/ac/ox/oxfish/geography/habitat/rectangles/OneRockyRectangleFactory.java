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

/**
 * A simplified factory that builds a single rectangle full of rocky fish
 * Created by carrknight on 11/18/15.
 */
public class OneRockyRectangleFactory
    implements AlgorithmFactory<RockyRectanglesHabitatInitializer> {


    private DoubleParameter topLeftX = new FixedDoubleParameter(0);
    private DoubleParameter topLeftY = new FixedDoubleParameter(0);

    private DoubleParameter width = new FixedDoubleParameter(5);
    private DoubleParameter height = new FixedDoubleParameter(5);

    public OneRockyRectangleFactory() {
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public RockyRectanglesHabitatInitializer apply(FishState fishState) {
        return new RockyRectanglesHabitatInitializer(
            (int) topLeftX.applyAsDouble(fishState.getRandom()),
            (int) topLeftY.applyAsDouble(fishState.getRandom()),
            (int) width.applyAsDouble(fishState.getRandom()),
            (int) height.applyAsDouble(fishState.getRandom())
        );
    }

    public DoubleParameter getTopLeftX() {
        return topLeftX;
    }

    public void setTopLeftX(DoubleParameter topLeftX) {
        this.topLeftX = topLeftX;
    }

    public DoubleParameter getTopLeftY() {
        return topLeftY;
    }

    public void setTopLeftY(DoubleParameter topLeftY) {
        this.topLeftY = topLeftY;
    }

    public DoubleParameter getWidth() {
        return width;
    }

    public void setWidth(DoubleParameter width) {
        this.width = width;
    }

    public DoubleParameter getHeight() {
        return height;
    }

    public void setHeight(DoubleParameter height) {
        this.height = height;
    }
}
