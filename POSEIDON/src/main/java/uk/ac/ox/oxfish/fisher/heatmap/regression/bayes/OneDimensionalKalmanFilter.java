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

package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * A simple one-dimensional Kalman filter
 * Created by carrknight on 8/2/16.
 */
public class OneDimensionalKalmanFilter {


    /**
     * the A of the model
     */
    private final double transitionMultiplier;

    /**
     * the C of the model (that is x*c = evidence)
     */
    private double emissionMultiplier;

    /**
     * the P of the kalman filter
     */
    private double uncertainty;

    /**
     * hat x of the kalman filter
     */
    private double stateEstimate;

    /**
     * the sigma_m of the kalman filter (the gaussian shock each time step to add to uncertainty)
     */
    private double drift;


    public OneDimensionalKalmanFilter(
        double transitionMultiplier, double emissionMultiplier, double uncertainty, double stateEstimate,
        double drift
    ) {
        this.transitionMultiplier = transitionMultiplier;
        this.emissionMultiplier = emissionMultiplier;
        this.uncertainty = uncertainty;
        this.stateEstimate = stateEstimate;
        this.drift = drift;
    }

    /**
     * when elapsing time we multiply the current estimate of the state by A and then we increase P by the Sigma_m
     */
    public void elapseTime() {
        stateEstimate = stateEstimate * transitionMultiplier;
        uncertainty = uncertainty * (transitionMultiplier * transitionMultiplier) + drift;
    }

    /**
     * updates state estimate with new evidence
     *
     * @param evidence            the measurement
     * @param evidenceUncertainty the Sigma_m of the kalman filter, that is the uncertainty we have regarding
     *                            the quality of the measurement
     */
    public void observe(double evidence, double evidenceUncertainty) {

        //weighs the importance of this new observation
        double kalmanGain = uncertainty * emissionMultiplier /
            (uncertainty * emissionMultiplier * emissionMultiplier + evidenceUncertainty);
        //update estimate in proportion to how far off the mark the prediction is (weighted by the kalman gain)
        stateEstimate = stateEstimate + kalmanGain * (evidence - emissionMultiplier * stateEstimate);
        //reduces uncertainty depending on the quality of the observation
        uncertainty = uncertainty - uncertainty * kalmanGain * emissionMultiplier;
    }


    /**
     * Getter for property 'uncertainty'.
     *
     * @return Value for property 'uncertainty'.
     */
    public double getUncertainty() {
        return uncertainty;
    }

    /**
     * Setter for property 'uncertainty'.
     *
     * @param uncertainty Value to set for property 'uncertainty'.
     */
    public void setUncertainty(double uncertainty) {
        this.uncertainty = uncertainty;
    }

    /**
     * Getter for property 'stateEstimate'.
     *
     * @return Value for property 'stateEstimate'.
     */
    public double getStateEstimate() {
        return stateEstimate;
    }

    /**
     * Setter for property 'stateEstimate'.
     *
     * @param stateEstimate Value to set for property 'stateEstimate'.
     */
    public void setStateEstimate(double stateEstimate) {
        this.stateEstimate = stateEstimate;
    }

    public double getProbabilityStateIsThis(double guess) {
        return FishStateUtilities.normalPDF(stateEstimate, getStandardDeviation()).apply(guess);
    }

    public double getStandardDeviation() {
        return Math.sqrt(uncertainty);
    }

    /**
     * Getter for property 'transitionMultiplier'.
     *
     * @return Value for property 'transitionMultiplier'.
     */
    public double getTransitionMultiplier() {
        return transitionMultiplier;
    }

    /**
     * Getter for property 'emissionMultiplier'.
     *
     * @return Value for property 'emissionMultiplier'.
     */
    public double getEmissionMultiplier() {
        return emissionMultiplier;
    }

    /**
     * Setter for property 'emissionMultiplier'.
     *
     * @param emissionMultiplier Value to set for property 'emissionMultiplier'.
     */
    public void setEmissionMultiplier(double emissionMultiplier) {
        this.emissionMultiplier = emissionMultiplier;
    }

    /**
     * Getter for property 'drift'.
     *
     * @return Value for property 'drift'.
     */
    public double getDrift() {
        return drift;
    }

    public void setDrift(double drift) {
        this.drift = drift;
    }
}
