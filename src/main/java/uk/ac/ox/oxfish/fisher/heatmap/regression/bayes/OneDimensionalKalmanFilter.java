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
    private final double emissionMultiplier;

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
    private final double drift;


    public OneDimensionalKalmanFilter(
            double transitionMultiplier, double emissionMultiplier, double uncertainty, double stateEstimate,
            double drift) {
        this.transitionMultiplier = transitionMultiplier;
        this.emissionMultiplier = emissionMultiplier;
        this.uncertainty = uncertainty;
        this.stateEstimate = stateEstimate;
        this.drift = drift;
    }

    /**
     * when elapsing time we multiply the current estimate of the state by A and then we increase P by the Sigma_m
     */
    public void elapseTime()
    {
        stateEstimate = stateEstimate * transitionMultiplier;
        uncertainty = uncertainty *(transitionMultiplier*transitionMultiplier)+drift;
    }

    /**
     * updates state estimate with new evidence
     * @param evidence the measurement
     * @param evidenceUncertainty the Sigma_m of the kalman filter, that is the uncertainty we have regarding
     *                            the quality of the measurement
     */
    public void observe(double evidence, double evidenceUncertainty)
    {

        //weighs the importance of this new observation
        double kalmanGain =  uncertainty * emissionMultiplier /
                (uncertainty * emissionMultiplier * emissionMultiplier +evidenceUncertainty);
        //update estimate in proportion to how far off the mark the prediction is (weighted by the kalman gain)
        stateEstimate = stateEstimate + kalmanGain *(evidence - emissionMultiplier*stateEstimate);
        //reduces uncertainty depending on the quality of the observation
        uncertainty = uncertainty - uncertainty * kalmanGain  * emissionMultiplier;
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
     * Getter for property 'stateEstimate'.
     *
     * @return Value for property 'stateEstimate'.
     */
    public double getStateEstimate() {
        return stateEstimate;
    }

    public double getStandardDeviation(){
        return Math.sqrt(uncertainty);
    }

    public double getProbabilityStateIsThis(double guess)
    {
        return FishStateUtilities.normalPDF(stateEstimate,getStandardDeviation()).apply(guess);
    }
}
