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

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * A kernel regression with a limited
 * Created by carrknight on 9/2/16.
 */
public class KernelRegression implements GeographicalRegression<Double> {


    /**
     * the rbf weight we give to each element
     */
    private final double[] bandwidths;

    /**
     * functions to extract features from the cell
     */
    private final ObservationExtractor[] extractors;


    /**
     * kernel object to use
     */
    private final RegressionDistance kernel;


    /**
     * delete observations after you have more than this
     */
    private final int maximumNumberOfObservationsToKeep;


    /**
     * observations
     */
    private final PriorityQueue<GeographicalObservation<Double>> observations;


    @SuppressWarnings("unchecked")
    public KernelRegression(
        final int maximumNumberOfObservationsToKeep,
        final RegressionDistance kernel,
        final Entry<ObservationExtractor, Double>... extractorsAndBandwidths
    ) {

        this.bandwidths = new double[extractorsAndBandwidths.length];
        this.extractors = new ObservationExtractor[extractorsAndBandwidths.length];
        this.kernel = kernel;


        for (int i = 0; i < extractorsAndBandwidths.length; i++) {
            this.extractors[i] = extractorsAndBandwidths[i].getKey();
            this.bandwidths[i] = extractorsAndBandwidths[i].getValue();
        }

        this.maximumNumberOfObservationsToKeep = maximumNumberOfObservationsToKeep;
        observations = new PriorityQueue<>(maximumNumberOfObservationsToKeep);
    }


    /**
     * adds an observation and if there are too many removes the oldest one
     *
     * @param observation
     * @param fisher
     * @param model
     */

    public void addObservation(
        final GeographicalObservation<Double> observation,
        final Fisher fisher,
        final FishState model
    ) {
        observations.add(observation);
        if (observations.size() > maximumNumberOfObservationsToKeep) {
            assert observations.size() == maximumNumberOfObservationsToKeep + 1;
            observations.poll();
        }
    }

    /**
     * turn the "V" value of the geographical observation into a number
     *
     * @param observation
     * @param fisher
     * @return
     */
    @Override
    public double extractNumericalYFromObservation(
        final GeographicalObservation<Double> observation, final Fisher fisher
    ) {
        return observation.getValue();
    }


    /**
     * predict numerical value here
     *
     * @param tile
     * @param time
     * @param fisher
     * @param model
     * @return
     */
    @Override
    public double predict(final SeaTile tile, final double time, final Fisher fisher, final FishState model) {


        double kernelSum = 0;
        double numerator = 0;
        for (final GeographicalObservation<Double> observation : observations) {
            double currentKernel = 1;
            for (int i = 0; i < bandwidths.length; i++) {
                kernel.setBandwidth(bandwidths[i]);
                currentKernel *= kernel.distance(
                    extractors[i].extract(tile, time, fisher, model),
                    extractors[i].extract(observation.getTile(),
                        observation.getTime(),
                        fisher, model
                    )
                );
                //don't bother if it's a 0
                if ((currentKernel) < .00001)
                    break;
            }

            if ((currentKernel) > .00001) {
                kernelSum += currentKernel;
                numerator += currentKernel * observation.getValue();
            }
        }

        if (kernelSum < .00001)
            return Double.NaN;

        return numerator / kernelSum;


    }


    @Override
    public void start(final FishState model, final Fisher fisher) {

    }

    @Override
    public void turnOff(final Fisher fisher) {

    }

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        return bandwidths;
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(final double[] parameterArray) {

        assert parameterArray.length == bandwidths.length;
        System.arraycopy(parameterArray, 0, bandwidths, 0, bandwidths.length);
    }


}
