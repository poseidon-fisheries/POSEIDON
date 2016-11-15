package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborRegressionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.tripbased.ProfitFunctionRegression;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.fisher.strategies.destination.HeatmapDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PlanningHeatmapDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;


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
    private AlgorithmFactory<? extends GeographicalRegression<Double>> regression =
            new NearestNeighborRegressionFactory();



    private boolean almostPerfectKnowledge = false;



    /**
     *
     */
    private AlgorithmFactory<? extends AcquisitionFunction> acquisition = new ExhaustiveAcquisitionFunctionFactory();


    /**
     * mantains a (weak) set of fish states so that we initialize our data gatherers only once!
     */
    private final Locker<FishState,String> locker = new Locker<>();

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PlanningHeatmapDestinationStrategy apply(FishState state) {

        //add data gathering if necessary
        if(!locker.getCurrentKey().equals(state))
        {
            locker.presentKey(state, () -> null);
            addDataGatherers(state);
            assert locker.getCurrentKey().equals(state);
        }

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
                    explorationStepSize.apply(state.getRandom()).intValue(),
                    state.getBiology()
            );
    }


    private void addDataGatherers(FishState state) {


        //first add data gatherers
        state.getYearlyDataSet().registerGatherer("Average Prediction Error",
                                                  model -> {
                                                      double size =model.getFishers().size();
                                                      if(size == 0)
                                                          return Double.NaN;
                                                      else
                                                      {
                                                          double total = 0;
                                                          for(Fisher fisher1 : state.getFishers()) {
                                                              DoubleSummaryStatistics errors = new DoubleSummaryStatistics();
                                                              for(Double error : ((HeatmapDestinationStrategy) fisher1.getDestinationStrategy()).getErrors())
                                                                  errors.accept(error);
                                                              total += errors.getAverage();
                                                          }
                                                          return total/size;
                                                      }
                                                  }, Double.NaN);

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
    public AlgorithmFactory<? extends GeographicalRegression<Double>> getRegression() {
        return regression;
    }

    /**
     * Setter for property 'regression'.
     *
     * @param regression Value to set for property 'regression'.
     */
    public void setRegression(
            AlgorithmFactory<? extends GeographicalRegression<Double>> regression) {
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
