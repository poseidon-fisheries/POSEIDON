package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborRegressionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NumericalGeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.tripbased.ProfitFunctionRegression;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.fisher.strategies.destination.PlanningHeatmapDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;



public class PlanningHeatmapDestinationFactory implements AlgorithmFactory<PlanningHeatmapDestinationStrategy>{



    private boolean ignoreFailedTrips = false;


    /**
     * step size when exploring
     */
    private DoubleParameter explorationStepSize = new UniformDoubleParameter(1, 10);

    /**
     * probability of exploring (imitating here means using other people observations as your own)
     */
    private AlgorithmFactory<? extends AdaptationProbability> probability =
            new FixedProbabilityFactory(.2,1d);

    /**
     * the regression object (used primarily for species regression)
     */
    private AlgorithmFactory<? extends NumericalGeographicalRegression> regression =
            new NearestNeighborRegressionFactory();



    private boolean almostPerfectKnowledge = false;



    /**
     *
     */
    private AlgorithmFactory<? extends AcquisitionFunction> acquisition = new ExhaustiveAcquisitionFunctionFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PlanningHeatmapDestinationStrategy apply(FishState state) {
        if(!almostPerfectKnowledge)
            return new PlanningHeatmapDestinationStrategy(
                    new ProfitFunctionRegression(
                            new ProfitFunction(24*5),
                            regression,
                            state
                    ),
                    acquisition.apply(state),
                    ignoreFailedTrips,
                    probability.apply(state),
                    state.getMap(),
                    state.getRandom(),
                    explorationStepSize.apply(state.getRandom()).intValue()
            );
        else
            return PlanningHeatmapDestinationStrategy.AlmostPerfectKnowledge(
                    24*5,
                    state.getSpecies().size(),
                    acquisition.apply(state),
                    ignoreFailedTrips,
                    probability.apply(state),
                    state.getMap(),
                    state.getRandom(),
                    explorationStepSize.apply(state.getRandom()).intValue()
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
     * Getter for property 'explorationStepSize'.
     *
     * @return Value for property 'explorationStepSize'.
     */
    public DoubleParameter getExplorationStepSize() {
        return explorationStepSize;
    }

    /**
     * Setter for property 'explorationStepSize'.
     *
     * @param explorationStepSize Value to set for property 'explorationStepSize'.
     */
    public void setExplorationStepSize(DoubleParameter explorationStepSize) {
        this.explorationStepSize = explorationStepSize;
    }


    /**
     * Getter for property 'regression'.
     *
     * @return Value for property 'regression'.
     */
    public AlgorithmFactory<? extends NumericalGeographicalRegression> getRegression() {
        return regression;
    }

    /**
     * Setter for property 'regression'.
     *
     * @param regression Value to set for property 'regression'.
     */
    public void setRegression(
            AlgorithmFactory<? extends NumericalGeographicalRegression> regression) {
        this.regression = regression;
    }

    /**
     * Getter for property 'acquisition'.
     *
     * @return Value for property 'acquisition'.
     */
    public AlgorithmFactory<? extends AcquisitionFunction> getAcquisition() {
        return acquisition;
    }

    /**
     * Setter for property 'acquisition'.
     *
     * @param acquisition Value to set for property 'acquisition'.
     */
    public void setAcquisition(
            AlgorithmFactory<? extends AcquisitionFunction> acquisition) {
        this.acquisition = acquisition;
    }

    /**
     * Getter for property 'almostPerfectKnowledge'.
     *
     * @return Value for property 'almostPerfectKnowledge'.
     */
    public boolean isAlmostPerfectKnowledge() {
        return almostPerfectKnowledge;
    }

    /**
     * Setter for property 'almostPerfectKnowledge'.
     *
     * @param almostPerfectKnowledge Value to set for property 'almostPerfectKnowledge'.
     */
    public void setAlmostPerfectKnowledge(boolean almostPerfectKnowledge) {
        this.almostPerfectKnowledge = almostPerfectKnowledge;
    }
}
