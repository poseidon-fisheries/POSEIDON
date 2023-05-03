/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.SullivanTransitionProbability;
import uk.ac.ox.oxfish.model.FishState;

public class LocalSullivanTransitionAging extends LocalAgingProcess {


    /**
     * one probability matrix for each subdivision of the species!
     */
    final private SullivanTransitionProbability[] transitionProbabilities;


    /**
     * after how many each days should the sullivan transition probability be applied?
     */
    final private int agingPeriodInDays;


    public LocalSullivanTransitionAging(
            SullivanTransitionProbability[] transitionProbabilities, int agingPeriodInDays) {
        this.transitionProbabilities = transitionProbabilities;
        this.agingPeriodInDays = agingPeriodInDays;
    }

    @Override
    public void ageLocally(
            AbundanceLocalBiology localBiology, Species species, FishState model, boolean rounding,
            int daysToSimulate) {
        assert  agingPeriodInDays % daysToSimulate == 0; //if it isn't a multiple then it's a problem!

        //step only when told
        if(model.getDay() % agingPeriodInDays != 0)
            return;

        Preconditions.checkArgument(rounding==false,
                                    "VariableProportionAging works very poorly with rounding!");
        Preconditions.checkArgument(species==speciesConnected,
                                    "Wrong species!");
        double[][] abundance = localBiology.getAbundance(species).asMatrix();

        //scale graduating proportion lazily

        for(int subdivision=0; subdivision<abundance.length; subdivision++)
        {
            Preconditions.checkArgument(transitionProbabilities[subdivision].getNumberOfBins() ==
                                                abundance[subdivision].length, "length mismatch between aging speed and # of bins");
            abundance[subdivision]= transition(
                    abundance[subdivision],
                    transitionProbabilities[subdivision].getTransitionMatrix()
            );
        }

    }

    private Species speciesConnected = null;

    /**
     * called after the aging process has been initialized but before it is run.
     *
     * @param species
     */
    @Override
    public void start(Species species) {
        Preconditions.checkState(speciesConnected==null);
        speciesConnected = species; //you don't want to re-use this for multiple species!!

    }


    /**
     * very simple helper: given an array of currentDistribution returns the new distribution
     *
     * @param currentDistribution
     * @return number of graduates (at position i is the number that left bin i and went into bin i+1)
     */
    private double[] transition(
            double[] currentDistribution,
            double[][] transitionMatrix
    )
    {
        int bins = currentDistribution.length;
        double[] nextDistribution = new double[bins];
        for(int departing = 0; departing< currentDistribution.length; departing++)
        {
            for (int arriving = departing; arriving < currentDistribution.length; arriving++)
            {
                nextDistribution[arriving] += currentDistribution[departing]*transitionMatrix[departing][arriving];
            }
        }
        assert noLostFish(currentDistribution,nextDistribution);

        return nextDistribution;

    }

    private boolean noLostFish(double[] current, double[] future){
        double sum1=0;
        double sum2=0;

        for(int i=0; i<current.length; i++)
        {
            sum1+=current[i];
            sum2+=future[i];
        }

        return Math.abs(sum1-sum2)<=.001;


    }

}
