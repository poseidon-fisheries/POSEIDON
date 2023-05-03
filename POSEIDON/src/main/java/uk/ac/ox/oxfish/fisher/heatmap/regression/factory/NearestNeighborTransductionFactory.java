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

import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.CartesianRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationTimeExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborTransduction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;


/**
 * Created by carrknight on 7/4/16.
 */
public class NearestNeighborTransductionFactory implements AlgorithmFactory<NearestNeighborTransduction> {


    private DoubleParameter timeBandwidth = new FixedDoubleParameter(1000d);


    private DoubleParameter spaceBandwidth = new FixedDoubleParameter(5d);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public NearestNeighborTransduction apply(final FishState state) {
        return new NearestNeighborTransduction(
            state.getMap(),
            new ObservationExtractor[]{
                new GridYExtractor(),
                new GridXExtractor(),
                new ObservationTimeExtractor()
            },
            new double[]{
                spaceBandwidth.applyAsDouble(state.getRandom()),
                spaceBandwidth.applyAsDouble(state.getRandom()),
                timeBandwidth.applyAsDouble(state.getRandom())
            },
            new CartesianRegressionDistance(0) //gets changed by the regression

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
    public void setTimeBandwidth(final DoubleParameter timeBandwidth) {
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
    public void setSpaceBandwidth(final DoubleParameter spaceBandwidth) {
        this.spaceBandwidth = spaceBandwidth;
    }


}
