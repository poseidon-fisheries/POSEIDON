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

package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates the Simple Map initializer
 * Created by carrknight on 11/5/15.
 */
public class SimpleMapInitializerFactory implements AlgorithmFactory<SimpleMapInitializer>{

    private DoubleParameter width = new FixedDoubleParameter(50);

    private DoubleParameter height= new FixedDoubleParameter(50);

    private DoubleParameter coastalRoughness= new FixedDoubleParameter(4);

    private DoubleParameter depthSmoothing= new FixedDoubleParameter(1000000);

    private DoubleParameter cellSizeInKilometers= new FixedDoubleParameter(10);

    private DoubleParameter maxLandWidth= new FixedDoubleParameter(10);


    public SimpleMapInitializerFactory() {
    }


    public SimpleMapInitializerFactory(
            int width, int height,
            int coastalRoughness, int depthSmoothing,
            double cellSizeInKilometers) {
        this.width = new FixedDoubleParameter(width);
        this.height = new FixedDoubleParameter(height);
        this.coastalRoughness = new FixedDoubleParameter(coastalRoughness);
        this.depthSmoothing = new FixedDoubleParameter(depthSmoothing);
        this.cellSizeInKilometers = new FixedDoubleParameter(cellSizeInKilometers);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public SimpleMapInitializer apply(FishState fishState) {
        return new SimpleMapInitializer(
                width.apply(fishState.getRandom()).intValue(),
                height.apply(fishState.getRandom()).intValue(),
                coastalRoughness.apply(fishState.getRandom()).intValue(),
                depthSmoothing.apply(fishState.getRandom()).intValue(),
                cellSizeInKilometers.apply(fishState.getRandom()),
                maxLandWidth.apply(fishState.getRandom()).intValue());

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

    public DoubleParameter getCoastalRoughness() {
        return coastalRoughness;
    }

    public void setCoastalRoughness(DoubleParameter coastalRoughness) {
        this.coastalRoughness = coastalRoughness;
    }

    public DoubleParameter getDepthSmoothing() {
        return depthSmoothing;
    }

    public void setDepthSmoothing(DoubleParameter depthSmoothing) {
        this.depthSmoothing = depthSmoothing;
    }

    public DoubleParameter getCellSizeInKilometers() {
        return cellSizeInKilometers;
    }

    public void setCellSizeInKilometers(DoubleParameter cellSizeInKilometers) {
        this.cellSizeInKilometers = cellSizeInKilometers;
    }

    /**
     * Getter for property 'maxLandWidth'.
     *
     * @return Value for property 'maxLandWidth'.
     */
    public DoubleParameter getMaxLandWidth() {
        return maxLandWidth;
    }

    /**
     * Setter for property 'maxLandWidth'.
     *
     * @param maxLandWidth Value to set for property 'maxLandWidth'.
     */
    public void setMaxLandWidth(DoubleParameter maxLandWidth) {
        this.maxLandWidth = maxLandWidth;
    }
}
