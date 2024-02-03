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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.HashSet;

/**
 * Create RandomCatchabilityTrawlGear
 * Created by carrknight on 9/30/15.
 */
public class RandomCatchabilityTrawlFactory implements AlgorithmFactory<RandomCatchabilityTrawl> {


    /**
     * here so that we know for which model we started gathering data
     */
    private final HashSet<FishState> models = new HashSet<>();
    private DoubleParameter meanCatchabilityFirstSpecies = new FixedDoubleParameter(.01);
    private DoubleParameter standardDeviationCatchabilityFirstSpecies = new FixedDoubleParameter(0);
    private DoubleParameter meanCatchabilityOtherSpecies = new FixedDoubleParameter(.01);
    private DoubleParameter standardDeviationCatchabilityOtherSpecies = new FixedDoubleParameter(0);
    private DoubleParameter gasPerHourFished = new FixedDoubleParameter(5);

    public RandomCatchabilityTrawlFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RandomCatchabilityTrawl apply(final FishState state) {

        final int species = state.getSpecies().size();
        final double[] means = new double[species];
        final double[] std = new double[species];

        means[0] = meanCatchabilityFirstSpecies.applyAsDouble(state.getRandom());
        std[0] = standardDeviationCatchabilityFirstSpecies.applyAsDouble(state.getRandom());

        for (int i = 1; i < means.length; i++) {
            means[i] = meanCatchabilityOtherSpecies.applyAsDouble(state.getRandom());
            std[i] = standardDeviationCatchabilityOtherSpecies.applyAsDouble(state.getRandom());
        }

        return new RandomCatchabilityTrawl(means, std, gasPerHourFished.applyAsDouble(state.getRandom()));


    }

    public DoubleParameter getMeanCatchabilityFirstSpecies() {
        return meanCatchabilityFirstSpecies;
    }

    public void setMeanCatchabilityFirstSpecies(
        final DoubleParameter meanCatchabilityFirstSpecies
    ) {
        this.meanCatchabilityFirstSpecies = meanCatchabilityFirstSpecies;
    }

    public DoubleParameter getStandardDeviationCatchabilityFirstSpecies() {
        return standardDeviationCatchabilityFirstSpecies;
    }

    public void setStandardDeviationCatchabilityFirstSpecies(
        final DoubleParameter standardDeviationCatchabilityFirstSpecies
    ) {
        this.standardDeviationCatchabilityFirstSpecies = standardDeviationCatchabilityFirstSpecies;
    }

    public DoubleParameter getMeanCatchabilityOtherSpecies() {
        return meanCatchabilityOtherSpecies;
    }

    public void setMeanCatchabilityOtherSpecies(
        final DoubleParameter meanCatchabilityOtherSpecies
    ) {
        this.meanCatchabilityOtherSpecies = meanCatchabilityOtherSpecies;
    }

    public DoubleParameter getStandardDeviationCatchabilityOtherSpecies() {
        return standardDeviationCatchabilityOtherSpecies;
    }

    public void setStandardDeviationCatchabilityOtherSpecies(
        final DoubleParameter standardDeviationCatchabilityOtherSpecies
    ) {
        this.standardDeviationCatchabilityOtherSpecies = standardDeviationCatchabilityOtherSpecies;
    }

    public DoubleParameter getGasPerHourFished() {
        return gasPerHourFished;
    }

    public void setGasPerHourFished(final DoubleParameter gasPerHourFished) {
        this.gasPerHourFished = gasPerHourFished;
    }


}
