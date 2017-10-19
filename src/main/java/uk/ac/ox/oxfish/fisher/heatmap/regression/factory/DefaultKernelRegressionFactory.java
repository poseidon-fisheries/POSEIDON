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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.KernelRegression;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 9/2/16.
 */
public class DefaultKernelRegressionFactory implements AlgorithmFactory<KernelRegression>{



    private DoubleParameter timeBandwidth = new FixedDoubleParameter(10000);

    private DoubleParameter numberOfObservations = new FixedDoubleParameter(50);

    private DoubleParameter xBandwidth = new FixedDoubleParameter(250);

    private DoubleParameter yBandwidth = new FixedDoubleParameter(250);

    private DoubleParameter distanceFromPortBandwidth = new FixedDoubleParameter(200);

    private DoubleParameter habitatBandwidth = new FixedDoubleParameter(1);

    private boolean rbfKernel = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public KernelRegression apply(FishState state) {

        return new KernelRegression(
                numberOfObservations.apply(state.getRandom()).intValue(),
                rbfKernel ? new RBFDistance(0) : new EpanechinikovKernel(0),
                new Pair<>(
                        new GridXExtractor(),
                        xBandwidth.apply(state.getRandom())
                ),
                new Pair<>(
                        new GridYExtractor(),
                        yBandwidth.apply(state.getRandom())
                ),
                new Pair<>(
                        new PortDistanceExtractor(new ManhattanDistance(), 1d),
                        distanceFromPortBandwidth.apply(state.getRandom())
                ),
                new Pair<>(
                        new HabitatExtractor(),
                        habitatBandwidth.apply(state.getRandom())
                ),
                new Pair<>(
                        new ObservationExtractor() {
                            @Override
                            public double extract(
                                    SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
                                return Math.sqrt(timeOfObservation+1);
                            }
                        },
                        timeBandwidth.apply(state.getRandom())
                )
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
     * Getter for property 'numberOfObservations'.
     *
     * @return Value for property 'numberOfObservations'.
     */
    public DoubleParameter getNumberOfObservations() {
        return numberOfObservations;
    }

    /**
     * Setter for property 'numberOfObservations'.
     *
     * @param numberOfObservations Value to set for property 'numberOfObservations'.
     */
    public void setNumberOfObservations(DoubleParameter numberOfObservations) {
        this.numberOfObservations = numberOfObservations;
    }

    /**
     * Getter for property 'xBandwidth'.
     *
     * @return Value for property 'xBandwidth'.
     */
    public DoubleParameter getxBandwidth() {
        return xBandwidth;
    }

    /**
     * Setter for property 'xBandwidth'.
     *
     * @param xBandwidth Value to set for property 'xBandwidth'.
     */
    public void setxBandwidth(DoubleParameter xBandwidth) {
        this.xBandwidth = xBandwidth;
    }

    /**
     * Getter for property 'yBandwidth'.
     *
     * @return Value for property 'yBandwidth'.
     */
    public DoubleParameter getyBandwidth() {
        return yBandwidth;
    }

    /**
     * Setter for property 'yBandwidth'.
     *
     * @param yBandwidth Value to set for property 'yBandwidth'.
     */
    public void setyBandwidth(DoubleParameter yBandwidth) {
        this.yBandwidth = yBandwidth;
    }

    /**
     * Getter for property 'distanceFromPortBandwidth'.
     *
     * @return Value for property 'distanceFromPortBandwidth'.
     */
    public DoubleParameter getDistanceFromPortBandwidth() {
        return distanceFromPortBandwidth;
    }

    /**
     * Setter for property 'distanceFromPortBandwidth'.
     *
     * @param distanceFromPortBandwidth Value to set for property 'distanceFromPortBandwidth'.
     */
    public void setDistanceFromPortBandwidth(DoubleParameter distanceFromPortBandwidth) {
        this.distanceFromPortBandwidth = distanceFromPortBandwidth;
    }

    /**
     * Getter for property 'habitatBandwidth'.
     *
     * @return Value for property 'habitatBandwidth'.
     */
    public DoubleParameter getHabitatBandwidth() {
        return habitatBandwidth;
    }

    /**
     * Setter for property 'habitatBandwidth'.
     *
     * @param habitatBandwidth Value to set for property 'habitatBandwidth'.
     */
    public void setHabitatBandwidth(DoubleParameter habitatBandwidth) {
        this.habitatBandwidth = habitatBandwidth;
    }

    /**
     * Getter for property 'rbfKernel'.
     *
     * @return Value for property 'rbfKernel'.
     */
    public boolean isRbfKernel() {
        return rbfKernel;
    }

    /**
     * Setter for property 'rbfKernel'.
     *
     * @param rbfKernel Value to set for property 'rbfKernel'.
     */
    public void setRbfKernel(boolean rbfKernel) {
        this.rbfKernel = rbfKernel;
    }
}
