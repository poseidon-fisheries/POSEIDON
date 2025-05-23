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

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.PriorityQueue;

/**
 * Created by carrknight on 6/28/16.
 */
public abstract class AbstractKernelRegression implements GeographicalRegression<Double> {


    private final int maximumNumberOfObservations;
    private final PriorityQueue<GeographicalObservation<Double>> observations;
    private double bandwidth;


    public AbstractKernelRegression(
        final int maximumNumberOfObservations,
        final double bandwidth
    ) {
        this.maximumNumberOfObservations = maximumNumberOfObservations;
        this.bandwidth = bandwidth;
        observations = new PriorityQueue<>(maximumNumberOfObservations);
    }

    /**
     * adds an observation and if there are too many removes the oldest one
     *
     * @param observation
     * @param fisher
     * @param model
     */
    @Override
    public void addObservation(
        final GeographicalObservation<Double> observation,
        final Fisher fisher,
        final FishState model
    ) {
        observations.add(observation);
        if (observations.size() > maximumNumberOfObservations) {
            assert observations.size() == maximumNumberOfObservations + 1;
            observations.poll();
        }
    }

    @Override
    public double predict(final SeaTile tile, final double time, final Fisher fisher, final FishState model) {

        if (tile.isLand())
            return Double.NaN;
        else
            return predict(tile.getGridX(), tile.getGridY(), time);
    }

    public Double predict(final int x, final int y, final double time) {

        if (getObservations().size() == 0)
            return 0d;
        if (getObservations().size() == 1)
            return getObservations().peek().getValue();


        final double[] prediction = generatePrediction(x, y, time);
        return prediction[0] / prediction[1];


    }

    /**
     * Getter for property 'observations'.
     *
     * @return Value for property 'observations'.
     */
    public PriorityQueue<GeographicalObservation<Double>> getObservations() {
        return observations;
    }

    /**
     * like predict but this one returns an array with [0] being the numerator and [1] being the kernel sum.
     * Useful if you need intermediate steps
     *
     * @param x    the gridX you are predicting at
     * @param y    the gridY you are predicting at
     * @param time the time (in hours) you want to predict to
     * @return an array with numerator and denominator
     */
    public double[] generatePrediction(final int x, final int y, final double time) {
        double kernelSum = 0;
        double numerator = 0;
        for (final GeographicalObservation<Double> observation : getObservations()) {
            final double distance = distance(
                observation.getXCoordinate(),
                observation.getYCoordinate(),
                observation.getTime(),
                x,
                y,
                time
            );
            final double kernel = kernel(distance / getBandwidth());
            kernelSum += kernel;
            numerator += kernel * observation.getValue();
        }

        return new double[]{numerator, kernelSum};

    }

    abstract protected double distance(
        double fromX, double fromY, double fromTime,
        double toX, double toY, double toTime
    );

    public double kernel(final double u) {
        return Math.max(1d / (Math.exp(u) + 2 + Math.exp(-u)), 0);
    }

    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getBandwidth() {
        return bandwidth;
    }

    /**
     * Setter for property 'bandwidth'.
     *
     * @param bandwidth Value to set for property 'bandwidth'.
     */
    public void setBandwidth(final double bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * Getter for property 'maximumNumberOfObservations'.
     *
     * @return Value for property 'maximumNumberOfObservations'.
     */
    public int getMaximumNumberOfObservations() {
        return maximumNumberOfObservations;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model, final Fisher fisher) {

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff(final Fisher fisher) {

    }

    /**
     * It's already a double so return it!
     */
    @Override
    public double extractNumericalYFromObservation(
        final GeographicalObservation<Double> observation, final Fisher fisher
    ) {
        return observation.getValue();
    }


    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        return new double[]{
            getBandwidth()};
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(final double[] parameterArray) {
        assert parameterArray.length == 1;

        setBandwidth(parameterArray[0]);

    }
}
