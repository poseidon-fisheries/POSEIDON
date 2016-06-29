package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.heatmap.HillClimberAcquisitionFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.heatmap.TimeAndSpaceKernelRegression;
import uk.ac.ox.oxfish.fisher.strategies.destination.HeatmapDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * Created by carrknight on 6/29/16.
 */
public class HeatmapDestinationFactory implements AlgorithmFactory<HeatmapDestinationStrategy>{


    private boolean ignoreFailedTrips = false;

    private AlgorithmFactory<? extends AdaptationProbability> probability =
            new FixedProbabilityFactory(.2,1d);

    private DoubleParameter stepSize = new UniformDoubleParameter(1,10);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HeatmapDestinationStrategy apply(FishState state) {
        return new HeatmapDestinationStrategy(
                new TimeAndSpaceKernelRegression(24*7,5,100),
                new HillClimberAcquisitionFunction(3),
                ignoreFailedTrips,
                probability.apply(state),
                state.getMap(),
                state.getRandom(),
                stepSize.apply(state.getRandom()).intValue()
        );
    }




    /**
     * Getter for property 'ignoreFailedTrips'.
     *
     * @return Value for property 'ignoreFailedTrips'.
     */
    public boolean isIgnoreFailedTrips() {
        return ignoreFailedTrips;
    }

    /**
     * Setter for property 'ignoreFailedTrips'.
     *
     * @param ignoreFailedTrips Value to set for property 'ignoreFailedTrips'.
     */
    public void setIgnoreFailedTrips(boolean ignoreFailedTrips) {
        this.ignoreFailedTrips = ignoreFailedTrips;
    }

    /**
     * Getter for property 'probability'.
     *
     * @return Value for property 'probability'.
     */
    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return probability;
    }

    /**
     * Setter for property 'probability'.
     *
     * @param probability Value to set for property 'probability'.
     */
    public void setProbability(
            AlgorithmFactory<? extends AdaptationProbability> probability) {
        this.probability = probability;
    }

    /**
     * Getter for property 'stepSize'.
     *
     * @return Value for property 'stepSize'.
     */
    public DoubleParameter getStepSize() {
        return stepSize;
    }

    /**
     * Setter for property 'stepSize'.
     *
     * @param stepSize Value to set for property 'stepSize'.
     */
    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }
}
