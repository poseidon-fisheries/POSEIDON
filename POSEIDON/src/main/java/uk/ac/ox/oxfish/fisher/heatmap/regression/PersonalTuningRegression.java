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

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * Just a gradient descent over latest prediction error
 * Created by carrknight on 9/13/16.
 */
public class PersonalTuningRegression implements GeographicalRegression<Double> {

    /**
     * the gradient is guessed numerically by checking prediction error at x +- percentageChangeToGuessGradient * x
     */
    private final double percentageChangeToGuessGradient;

    /**
     * the alpha/gamma that is by how much we change our current parameters given the latest gradient
     */
    private final double stepSize;


    private final GeographicalRegression<Double> delegate;

    /**
     * how many observations to wait before start tuning
     */
    private final int observationsBeforeTuning;


    private int numberOfObservations = 0;


    public PersonalTuningRegression(
        GeographicalRegression<Double> delegate, double percentageChangeToGuessGradient, double stepSize,
        int observationsBeforeTuning
    ) {
        this.percentageChangeToGuessGradient = percentageChangeToGuessGradient;
        this.stepSize = stepSize;
        this.delegate = delegate;
        this.observationsBeforeTuning = observationsBeforeTuning;
    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * delegate
     */
    @Override
    public double predict(SeaTile tile, double time, Fisher fisher, FishState model) {


        return delegate.predict(tile, time, fisher, model);
    }

    /**
     * learn from this observation
     *
     * @param observation
     * @param fisher
     * @param model
     */
    @Override
    public void addObservation(
        GeographicalObservation<Double> observation, Fisher fisher, FishState model
    ) {

        numberOfObservations++;
        if (numberOfObservations > observationsBeforeTuning)
            tune(observation, fisher, model);
        delegate.addObservation(observation, fisher, model);


    }

    /**
     * performs a one step gradient descent
     */
    private void tune(
        GeographicalObservation<Double> observation,
        Fisher fisher, FishState model
    ) {

        //find (squared) prediction error
        double predictionError = delegate.predict(observation.getTile(), observation.getTime(), fisher, model) -
            extractNumericalYFromObservation(observation, fisher);
        predictionError *= predictionError;
        if (!Double.isFinite(predictionError))
            return;


        //loop through the parameters
        double[] parameters = Arrays.copyOf(delegate.getParametersAsArray(), delegate.getParametersAsArray().length);
        double[] gradient = new double[parameters.length];
        for (int i = 0; i < gradient.length; i++) {
            double[] highParameters = Arrays.copyOf(parameters, parameters.length);
            highParameters[i] *= 1 + percentageChangeToGuessGradient;
            double h = highParameters[i] - parameters[i];
            //too small of a change? don't bother
            if (h == 0) {
                assert highParameters[i] == parameters[i];
                h = FishStateUtilities.EPSILON;
                highParameters[i] += h;

            }

            delegate.setParameters(highParameters);
            double errorHigh = delegate.predict(observation.getTile(), observation.getTime(), fisher, model) -
                extractNumericalYFromObservation(observation, fisher);

            gradient[i] = (errorHigh * errorHigh - predictionError) / h;

        }

        //modify parameters
        for (int i = 0; i < parameters.length; i++)
            if (Double.isFinite(gradient[i]))
                parameters[i] = parameters[i] - stepSize * gradient[i];

        delegate.setParameters(parameters);

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
        GeographicalObservation<Double> observation, Fisher fisher
    ) {
        return delegate.extractNumericalYFromObservation(observation, fisher);
    }

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        return delegate.getParametersAsArray();
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(double[] parameterArray) {
        delegate.setParameters(parameterArray);
    }
}
