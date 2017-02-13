package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.HabitatExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.KernelTransduction;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Simple Kernel transduction all using rbf values
 * Created by carrknight on 8/15/16.
 */
public class DefaultRBFKernelTransductionFactory implements AlgorithmFactory<KernelTransduction> {



    private DoubleParameter forgettingFactor = new FixedDoubleParameter(.95);

    private DoubleParameter xBandwidth = new FixedDoubleParameter(25);

    private DoubleParameter yBandwidth = new FixedDoubleParameter(25);

    private DoubleParameter distanceFromPortBandwidth = new FixedDoubleParameter(25);

    private DoubleParameter habitatBandwidth = new FixedDoubleParameter(1);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public KernelTransduction apply(FishState state) {

        return new KernelTransduction(
                state.getMap(),
                forgettingFactor.apply(state.getRandom()),
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
                ));



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
}
