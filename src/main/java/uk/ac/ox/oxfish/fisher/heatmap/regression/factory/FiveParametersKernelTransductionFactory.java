package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.DefaultKernelTransduction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.HabitatRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.PortDifferenceRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RandomRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.SpaceRegressionDistance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/8/16.
 */
public class FiveParametersKernelTransductionFactory implements AlgorithmFactory<DefaultKernelTransduction>{


    private DoubleParameter forgettingFactor = new FixedDoubleParameter(.95);

    private DoubleParameter spaceBandwidth = new FixedDoubleParameter(5);

    private DoubleParameter distanceFromPortBandwidth = new FixedDoubleParameter(1);

    private DoubleParameter habitatBandwidth = new FixedDoubleParameter(1);

    private DoubleParameter randomBandwidth = new FixedDoubleParameter(100000);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DefaultKernelTransduction apply(FishState state) {

        return new DefaultKernelTransduction(
                state.getMap(),
                forgettingFactor.apply(state.getRandom()),
                new SpaceRegressionDistance(spaceBandwidth.apply(state.getRandom())),
                new PortDifferenceRegressionDistance(distanceFromPortBandwidth.apply(state.getRandom())),
                new HabitatRegressionDistance(habitatBandwidth.apply(state.getRandom())),
                new RandomRegressionDistance(1/randomBandwidth.apply(state.getRandom()),state.getRandom()));



    }

    /**
     * Getter for property 'forgettingFactor'.
     *
     * @return Value for property 'forgettingFactor'.
     */
    public DoubleParameter getForgettingFactor() {
        return forgettingFactor;
    }

    /**
     * Setter for property 'forgettingFactor'.
     *
     * @param forgettingFactor Value to set for property 'forgettingFactor'.
     */
    public void setForgettingFactor(DoubleParameter forgettingFactor) {
        this.forgettingFactor = forgettingFactor;
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
     * Getter for property 'randomBandwidth'.
     *
     * @return Value for property 'randomBandwidth'.
     */
    public DoubleParameter getRandomBandwidth() {
        return randomBandwidth;
    }

    /**
     * Setter for property 'randomBandwidth'.
     *
     * @param randomBandwidth Value to set for property 'randomBandwidth'.
     */
    public void setRandomBandwidth(DoubleParameter randomBandwidth) {
        this.randomBandwidth = randomBandwidth;
    }

}
