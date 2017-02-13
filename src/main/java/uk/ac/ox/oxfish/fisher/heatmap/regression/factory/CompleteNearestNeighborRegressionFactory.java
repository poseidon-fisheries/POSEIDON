package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborRegression;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * 5d regression tree
 * Created by carrknight on 8/18/16.
 */
public class CompleteNearestNeighborRegressionFactory implements AlgorithmFactory<NearestNeighborRegression> {





        private DoubleParameter timeBandwidth = new FixedDoubleParameter(5000d);


        private DoubleParameter xBandwidth = new FixedDoubleParameter(.5d);

        private DoubleParameter yBandwidth = new FixedDoubleParameter(.5d);

        private DoubleParameter distanceFromPortBandwidth = new FixedDoubleParameter(.5d);

        private DoubleParameter habitatBandwidth = new FixedDoubleParameter(1d);


        private final static ObservationExtractor[] extractors = new ObservationExtractor[5];
        static {
            extractors[0] = new ObservationTimeExtractor();
            extractors[1] = new GridXExtractor();
            extractors[2] = new GridYExtractor();
            ManhattanDistance distance = new ManhattanDistance();
            extractors[3] = new PortDistanceExtractor(distance, 1d);
            extractors[4] = new HabitatExtractor();

        }


        private DoubleParameter neighbors= new FixedDoubleParameter(1d);


        /**
         * Applies this function to the given argument.
         *
         * @param state the function argument
         * @return the function result
         */
        @Override
        public NearestNeighborRegression apply(FishState state) {

            double bandwidths[] = new double[5];
            bandwidths[0] = timeBandwidth.apply(state.getRandom());
            bandwidths[1] = xBandwidth.apply(state.getRandom());
            bandwidths[2] = yBandwidth.apply(state.getRandom());
            bandwidths[3] = distanceFromPortBandwidth.apply(state.getRandom());
            bandwidths[4] = habitatBandwidth.apply(state.getRandom());

            return new NearestNeighborRegression(
                    neighbors.apply(state.getRandom()).intValue(),
                    bandwidths,
                    extractors
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
     * Getter for property 'extractors'.
     *
     * @return Value for property 'extractors'.
     */
    public static ObservationExtractor[] getExtractors() {
        return extractors;
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
