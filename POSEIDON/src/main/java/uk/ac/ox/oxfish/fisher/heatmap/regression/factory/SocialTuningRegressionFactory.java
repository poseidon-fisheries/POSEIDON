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
import uk.ac.ox.oxfish.fisher.heatmap.regression.SocialTuningRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.strategies.destination.AbstractHeatmapDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
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
 * Created by carrknight on 8/26/16.
 */
public class SocialTuningRegressionFactory implements AlgorithmFactory<SocialTuningRegression<Double>> {


    /**
     * mantains a (weak) set of fish states so that we initialize our data gatherers only once!
     */
    private final Set<FishState> weakStateMap = Collections.newSetFromMap(new WeakHashMap<>());
    private AlgorithmFactory<? extends GeographicalRegression<Double>> nested = new CompleteNearestNeighborRegressionFactory();
    private boolean yearly = false;

    private AlgorithmFactory<? extends AdaptationProbability> probability = new FixedProbabilityFactory(.2, 1);

    {
        ((CompleteNearestNeighborRegressionFactory) nested).setDistanceFromPortBandwidth(new UniformDoubleParameter(
            0.1,
            1000
        ));
        ((CompleteNearestNeighborRegressionFactory) nested).setHabitatBandwidth(new UniformDoubleParameter(0.1, 1000));
        ((CompleteNearestNeighborRegressionFactory) nested).setTimeBandwidth(new UniformDoubleParameter(0.1, 1000));
        ((CompleteNearestNeighborRegressionFactory) nested).setxBandwidth(new UniformDoubleParameter(0.1, 1000));
        ((CompleteNearestNeighborRegressionFactory) nested).setyBandwidth(new UniformDoubleParameter(0.1, 1000));
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SocialTuningRegression<Double> apply(final FishState state) {


        final GeographicalRegression<Double> delegate = this.nested.apply(state);
        final DoubleParameter[] zeros = new DoubleParameter[delegate.getParametersAsArray().length];
        Arrays.fill(zeros, new FixedDoubleParameter(0));


        //add data gathering if necessary
        if (!weakStateMap.contains(state)) {
            weakStateMap.add(state);
            addDataGatherers(state, zeros.length);
            assert weakStateMap.contains(state);
        }

        return new SocialTuningRegression<>(
            delegate,
            probability.apply(state),
            yearly
        );

    }

    private void addDataGatherers(final FishState state, final int length) {


        for (int i = 0; i < length; i++) {

            //first add data gatherers
            final int finalI = i;
            state.
                getYearlyDataSet().
                registerGatherer("Average Heatmap Parameter " + i,
                    model -> {
                        final double size = model.getFishers().size();
                        if (size == 0)
                            return Double.NaN;
                        else {
                            double total = 0;
                            for (final Fisher fisher1 : state.getFishers()) {
                                total +=
                                    ((AbstractHeatmapDestinationStrategy) fisher1.getDestinationStrategy()).
                                        getHeatmap().getParametersAsArray()[finalI];
                            }
                            return total / size;
                        }
                    }, Double.NaN
                );
        }

    }


    /**
     * Getter for property 'nested'.
     *
     * @return Value for property 'nested'.
     */
    public AlgorithmFactory<? extends GeographicalRegression<Double>> getNested() {
        return nested;
    }

    /**
     * Setter for property 'nested'.
     *
     * @param nested Value to set for property 'nested'.
     */
    public void setNested(
        final AlgorithmFactory<? extends GeographicalRegression<Double>> nested
    ) {
        this.nested = nested;
    }

    /**
     * Getter for property 'yearly'.
     *
     * @return Value for property 'yearly'.
     */
    public boolean isYearly() {
        return yearly;
    }

    /**
     * Setter for property 'yearly'.
     *
     * @param yearly Value to set for property 'yearly'.
     */
    public void setYearly(final boolean yearly) {
        this.yearly = yearly;
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
        final AlgorithmFactory<? extends AdaptationProbability> probability
    ) {
        this.probability = probability;
    }
}
