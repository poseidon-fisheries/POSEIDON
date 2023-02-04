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

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.PersonalTuningRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.SocialTuningRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.strategies.destination.HeatmapDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by carrknight on 9/13/16.
 */
public class PersonalTuningRegressionFactory implements AlgorithmFactory<PersonalTuningRegression>{


    private AlgorithmFactory<? extends GeographicalRegression> nested = new DefaultKernelRegressionFactory();
    {
        ((DefaultKernelRegressionFactory) nested).setDistanceFromPortBandwidth(new UniformDoubleParameter(0.1, 1000));
        ((DefaultKernelRegressionFactory) nested).setHabitatBandwidth(new UniformDoubleParameter(0.1,1000));
        ((DefaultKernelRegressionFactory) nested).setTimeBandwidth(new UniformDoubleParameter(100,10000));
        ((DefaultKernelRegressionFactory) nested).setxBandwidth(new UniformDoubleParameter(0.1,1000));
        ((DefaultKernelRegressionFactory) nested).setyBandwidth(new UniformDoubleParameter(0.1,1000));
    }




    /**
     * the gradient is guessed numerically by checking prediction error at x +- percentageChangeToGuessGradient * x
     */
    private DoubleParameter percentageChangeToGuessGradient = new FixedDoubleParameter(.01);

    /**
     * the alpha/gamma that is by how much we change our current parameters given the latest gradient
     */
    private DoubleParameter stepSize =  new FixedDoubleParameter(.01);




    /**
     * mantains a (weak) set of fish states so that we initialize our data gatherers only once!
     */
    private final Set<FishState> weakStateMap = Collections.newSetFromMap(new WeakHashMap<>());



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PersonalTuningRegression apply(FishState state) {



        GeographicalRegression delegate = this.nested.apply(state);
        DoubleParameter[] zeros = new DoubleParameter[delegate.getParametersAsArray().length];
        Arrays.fill(zeros, new FixedDoubleParameter(0));


        //add data gathering if necessary
        if(!weakStateMap.contains(state))
        {
            weakStateMap.add(state);
            addDataGatherers(state,zeros.length);
            assert weakStateMap.contains(state);
        }

        return new PersonalTuningRegression(
                delegate,
                percentageChangeToGuessGradient.apply(state.getRandom()),
                stepSize.apply(state.getRandom()),
                50);

    }

    private void addDataGatherers(FishState state, int length) {


        for(int i=0; i<length; i++)
        {

            //first add data gatherers
            int finalI = i;
            Gatherer<FishState> gatherer = model -> {
                double size = model.getFishers().size();
                if (size == 0)
                    return Double.NaN;
                else {
                    double total = 0;
                    for (Fisher fisher1 : state.getFishers()) {
                        total +=
                                ((HeatmapDestinationStrategy) fisher1.getDestinationStrategy()).
                                        getHeatmap().getParametersAsArray()[finalI];
                    }
                    return total / size;
                }
            };
            state.
                    getDailyDataSet().
                    registerGatherer("Average Heatmap Parameter " + i,
                                     gatherer, Double.NaN);
            state.
                    getYearlyDataSet().
                    registerGatherer("Average Heatmap Parameter " + i,
                                     gatherer, Double.NaN);
        }

    }


    /**
     * Getter for property 'nested'.
     *
     * @return Value for property 'nested'.
     */
    public AlgorithmFactory<? extends GeographicalRegression> getNested() {
        return nested;
    }

    /**
     * Setter for property 'nested'.
     *
     * @param nested Value to set for property 'nested'.
     */
    public void setNested(
            AlgorithmFactory<? extends GeographicalRegression> nested) {
        this.nested = nested;
    }


    /**
     * Getter for property 'percentageChangeToGuessGradient'.
     *
     * @return Value for property 'percentageChangeToGuessGradient'.
     */
    public DoubleParameter getPercentageChangeToGuessGradient() {
        return percentageChangeToGuessGradient;
    }

    /**
     * Setter for property 'percentageChangeToGuessGradient'.
     *
     * @param percentageChangeToGuessGradient Value to set for property 'percentageChangeToGuessGradient'.
     */
    public void setPercentageChangeToGuessGradient(
            DoubleParameter percentageChangeToGuessGradient) {
        this.percentageChangeToGuessGradient = percentageChangeToGuessGradient;
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
