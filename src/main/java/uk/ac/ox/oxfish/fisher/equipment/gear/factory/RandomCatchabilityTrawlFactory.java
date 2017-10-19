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
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashSet;

/**
 * Create RandomCatchabilityTrawlGear
 * Created by carrknight on 9/30/15.
 */
public class RandomCatchabilityTrawlFactory implements AlgorithmFactory<RandomCatchabilityTrawl>
{


    private DoubleParameter meanCatchabilityFirstSpecies = new FixedDoubleParameter(.01);

    private DoubleParameter standardDeviationCatchabilityFirstSpecies = new FixedDoubleParameter(0);


    private DoubleParameter meanCatchabilityOtherSpecies = new FixedDoubleParameter(.01);

    private DoubleParameter standardDeviationCatchabilityOtherSpecies = new FixedDoubleParameter(0);


    private DoubleParameter gasPerHourFished = new FixedDoubleParameter(5);

    /**
     * here so that we know for which model we started gathering data
     */
    private final HashSet<FishState> models = new HashSet<>();

    public RandomCatchabilityTrawlFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RandomCatchabilityTrawl apply(FishState state) {

        int species = state.getSpecies().size();
        double[] means = new double[species];
        double[] std = new double[species];

        means[0] = meanCatchabilityFirstSpecies.apply(state.getRandom());
        std[0] = standardDeviationCatchabilityFirstSpecies.apply(state.getRandom());

        for(int i=1; i<means.length; i++)
        {
            means[i] = meanCatchabilityOtherSpecies.apply(state.getRandom());
            std[i] = standardDeviationCatchabilityOtherSpecies.apply(state.getRandom());
        }

        return new RandomCatchabilityTrawl(means, std, gasPerHourFished.apply(state.getRandom()));


    }

    public DoubleParameter getMeanCatchabilityFirstSpecies() {
        return meanCatchabilityFirstSpecies;
    }

    public void setMeanCatchabilityFirstSpecies(
            DoubleParameter meanCatchabilityFirstSpecies) {
        this.meanCatchabilityFirstSpecies = meanCatchabilityFirstSpecies;
    }

    public DoubleParameter getStandardDeviationCatchabilityFirstSpecies() {
        return standardDeviationCatchabilityFirstSpecies;
    }

    public void setStandardDeviationCatchabilityFirstSpecies(
            DoubleParameter standardDeviationCatchabilityFirstSpecies) {
        this.standardDeviationCatchabilityFirstSpecies = standardDeviationCatchabilityFirstSpecies;
    }

    public DoubleParameter getMeanCatchabilityOtherSpecies() {
        return meanCatchabilityOtherSpecies;
    }

    public void setMeanCatchabilityOtherSpecies(
            DoubleParameter meanCatchabilityOtherSpecies) {
        this.meanCatchabilityOtherSpecies = meanCatchabilityOtherSpecies;
    }

    public DoubleParameter getStandardDeviationCatchabilityOtherSpecies() {
        return standardDeviationCatchabilityOtherSpecies;
    }

    public void setStandardDeviationCatchabilityOtherSpecies(
            DoubleParameter standardDeviationCatchabilityOtherSpecies) {
        this.standardDeviationCatchabilityOtherSpecies = standardDeviationCatchabilityOtherSpecies;
    }

    public DoubleParameter getGasPerHourFished() {
        return gasPerHourFished;
    }

    public void setGasPerHourFished(DoubleParameter gasPerHourFished) {
        this.gasPerHourFished = gasPerHourFished;
    }


}
