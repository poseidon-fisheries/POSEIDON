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

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/4/16.
 */
public class NearestNeighborRegressionFactory implements AlgorithmFactory<NearestNeighborRegression> {


    private DoubleParameter timeBandwidth = new FixedDoubleParameter(500d);


    private DoubleParameter spaceBandwidth = new FixedDoubleParameter(5d);


    private DoubleParameter neighbors= new FixedDoubleParameter(1d);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public NearestNeighborRegression apply(FishState state) {
        return new NearestNeighborRegression(
                neighbors.apply(state.getRandom()).intValue(),
                timeBandwidth.apply(state.getRandom()),
                spaceBandwidth.apply(state.getRandom())
        );
    }


    /**
     * Getter for property 'timeBandwidth'.
     *
     * @return Value for property 'timeBandwidth'.
     */
    public DoubleParameter getTimeBandwidth() {
        return timeBandwidth;
    }

    /**
     * Setter for property 'timeBandwidth'.
     *
     * @param timeBandwidth Value to set for property 'timeBandwidth'.
     */
    public void setTimeBandwidth(DoubleParameter timeBandwidth) {
        this.timeBandwidth = timeBandwidth;
    }

    /**
     * Getter for property 'spaceBandwidth'.
     *
     * @return Value for property 'spaceBandwidth'.
     */
    public DoubleParameter getSpaceBandwidth() {
        return spaceBandwidth;
    }

    /**
     * Setter for property 'spaceBandwidth'.
     *
     * @param spaceBandwidth Value to set for property 'spaceBandwidth'.
     */
    public void setSpaceBandwidth(DoubleParameter spaceBandwidth) {
        this.spaceBandwidth = spaceBandwidth;
    }

    /**
     * Getter for property 'neighbors'.
     *
     * @return Value for property 'neighbors'.
     */
    public DoubleParameter getNeighbors() {
        return neighbors;
    }

    /**
     * Setter for property 'neighbors'.
     *
     * @param neighbors Value to set for property 'neighbors'.
     */
    public void setNeighbors(DoubleParameter neighbors) {
        this.neighbors = neighbors;
    }
}
