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

package uk.ac.ox.oxfish.biology.boxcars;

import com.google.common.annotations.VisibleForTesting;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.LocalAgingProcess;
import uk.ac.ox.oxfish.biology.complicated.VariableProportionAging;
import uk.ac.ox.oxfish.model.FishState;

public class FixedBoxcarAging  extends LocalAgingProcess {



    private final double k;

    private final double LInfinity;


    public FixedBoxcarAging(double k, double LInfinity) {
        this.k = k;
        this.LInfinity = LInfinity;
    }

    private VariableProportionAging delegate;


    /**
     * called after the aging process has been initialized but before it is run.
     *
     * @param species
     */
    @Override
    public void start(Species species) {

        int numberOfBins = species.getMeristics().getNumberOfBins();
        double[][] graduatingRates = new double[species.getNumberOfSubdivisions()][numberOfBins];


        for(int subdivision = 0; subdivision<graduatingRates.length; subdivision++) {
            double[] proportionGraduating = graduatingRates[subdivision];
            //this is just the derivative of VB per time
            double[] growthPerBin = new double[numberOfBins];
            for (int i = 0; i < numberOfBins; i++)
                growthPerBin[i] = Math.max(k * (LInfinity - species.getLength(subdivision,i)),0);
            //turn this into graduating proportion
            //which is basically what % of length distance has been covered within deltaT (by growthPerBin)
            for (int i = 0; i < numberOfBins - 1; i++)
                proportionGraduating[i] =
                        Math.max(growthPerBin[i] /
                                         (species.getLength(subdivision,i + 1)
                                                 - species.getLength(subdivision,i)), 0);
            proportionGraduating[numberOfBins - 1] = 0;
        }


        this.delegate = new VariableProportionAging(graduatingRates);


    }


    /**
     * as a side-effect ages the local biology according to its rules
     *
     * @param localBiology
     * @param species
     * @param model
     * @param rounding
     * @param daysToSimulate
     */
    @Override
    public void ageLocally(
            AbundanceLocalBiology localBiology, Species species, FishState model, boolean rounding,
            int daysToSimulate) {
        delegate.ageLocally(localBiology, species, model, rounding, daysToSimulate);
    }

    /**
     * Getter for property 'yearlyProportionGraduating'.
     *
     * @return Value for property 'yearlyProportionGraduating'.
     * @param subdivision
     */
    @VisibleForTesting
    public double[] getYearlyProportionGraduating(int subdivision) {
        return delegate.getYearlyProportionGraduating(subdivision);
    }
}
