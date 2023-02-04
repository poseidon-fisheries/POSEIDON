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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborRegressionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.HourlyProfitObjectiveFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.HeatmapDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.Set;
import java.util.WeakHashMap;


public class HeatmapDestinationFactory implements AlgorithmFactory<HeatmapDestinationStrategy>{



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
     * the regression object (the one that builds the actual heatmap)
     */
    private AlgorithmFactory<? extends GeographicalRegression<Double>> regression =
            new NearestNeighborRegressionFactory();

    /**
     *
     */
    private AlgorithmFactory<? extends AcquisitionFunction> acquisition = new ExhaustiveAcquisitionFunctionFactory();


    /**
     * mantains a (weak) set of fish states so that we initialize our data gatherers only once!
     */
    private final Set<FishState> weakStateMap = Collections.newSetFromMap(new WeakHashMap<>());


    private AlgorithmFactory<? extends ObjectiveFunction<Fisher>> objectiveFunction =
            new HourlyProfitObjectiveFactory(true);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HeatmapDestinationStrategy apply(FishState state) {


        //add data gathering if necessary
        if(!weakStateMap.contains(state))
        {
            weakStateMap.add(state);
            addDataGatherers(state);
            assert weakStateMap.contains(state);
        }

        return new HeatmapDestinationStrategy(
                regression.apply(state),
                acquisition.apply(state),
                ignoreFailedTrips,
                probability.apply(state),
                state.getMap(),
                state.getRandom(),
                explorationStepSize.apply(state.getRandom()).intValue(),
                objectiveFunction.apply(state));
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
     * Getter for property 'objectiveFunction'.
     *
     * @return Value for property 'objectiveFunction'.
     */
    public AlgorithmFactory<? extends ObjectiveFunction<Fisher>> getObjectiveFunction() {
        return objectiveFunction;
    }

    /**
     * Setter for property 'objectiveFunction'.
     *
     * @param objectiveFunction Value to set for property 'objectiveFunction'.
     */
    public void setObjectiveFunction(
            AlgorithmFactory<? extends ObjectiveFunction<Fisher>> objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }
}
