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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.model.FishState;

/**
 * aging as simulated by a fixed boxcar process
 */
public class VariableProportionAging implements AgingProcess {


    /**
     * the proportion graduating each step
     */
    private final double[][] yearlyProportionGraduating;

    /**
     * keep track of proportion graduating in the time step last called; makes things faster
     */
    private double[][] lazyGraduation = null;
    private int daysToSimulateLastLazyEvaluation = -1;


    /**
     * called after the aging process has been initialized but before it is run.
     *
     * @param species
     */
    @Override
    public void initialize(Species species) {

    }

    public VariableProportionAging(double[][] yearlyProportionGraduating) {
        this.yearlyProportionGraduating = yearlyProportionGraduating;
    }


    /**
     * as a side-effect ages the local biology according to its rules
     * @param localBiology
     * @param species
     * @param model
     * @param rounding
     * @param daysToSimulate
     */
    @Override
    public void ageLocally(
            AbundanceBasedLocalBiology localBiology, Species species, FishState model, boolean rounding,
            int daysToSimulate)
    {

        Preconditions.checkArgument(rounding==false,
                                    "VariableProportionAging works very poorly with rounding!");
        double[][] abundance = localBiology.getAbundance(species).asMatrix();

        //scale graduating proportion lazily
        lazyEvaluation(daysToSimulate);
        assert daysToSimulateLastLazyEvaluation == daysToSimulate;

        for(int subdivision=0; subdivision<abundance.length; subdivision++)
        {
            Preconditions.checkArgument(yearlyProportionGraduating[subdivision].length ==
                                                abundance[subdivision].length, "length mismatch between aging speed and # of bins");
            variableAging(
                    abundance[subdivision],
                    lazyGraduation[subdivision]
            );
        }


    }


    /**
     * quick helper to avoid rescaling the same vector by the same factor every time
     */
    private void lazyEvaluation(int daysToSimulate){
        if(daysToSimulate==daysToSimulateLastLazyEvaluation)
        {
            assert lazyGraduation !=null;
        }
        else
        {
            lazyGraduation = new double[yearlyProportionGraduating.length][yearlyProportionGraduating[0].length];
            for(int i = 0; i< lazyGraduation.length; i++)
            {
                for(int j=0; j<lazyGraduation[i].length; j++)
                lazyGraduation[i][j] = yearlyProportionGraduating[i][j] * ((double)daysToSimulate)/365d;
            }
            this.daysToSimulateLastLazyEvaluation = daysToSimulate;
        }
    }



    /**
     * very simple helper: given an array of currentDistribution,
     * graduates a proportion of each to the next bin;
     * CurrentDistribution is changed as a **side effect**
     *
     * @param currentDistribution
     * @param proportionGraduating
     * @return number of graduates (at position i is the number that left bin i and went into bin i+1)
     */
    public static double[] variableAging(
            double[] currentDistribution,
            double[] proportionGraduating
    )
    {
        int bins = currentDistribution.length;
        double[] graduate = new double[bins];
        Preconditions.checkArgument(bins ==
                                            proportionGraduating.length);
        Preconditions.checkArgument(bins >2);
        assert proportionGraduating[bins-1]==0 || Double.isNaN(proportionGraduating[bins-1]);
        //going backward
        for(int i=bins-2; i>=0; i--)
        {
            assert currentDistribution[i] >=0;
            // find graduates
            graduate[i] = currentDistribution[i]*proportionGraduating[i];
            currentDistribution[i+1] += graduate[i];
            currentDistribution[i] -= graduate[i];
            assert currentDistribution[i] >=0;

        }
        return graduate;

    }

    /**
     * Getter for property 'yearlyProportionGraduating'.
     *
     * @return Value for property 'yearlyProportionGraduating'.
     */
    @VisibleForTesting
    public double[] getYearlyProportionGraduating(int subdivision) {
        return yearlyProportionGraduating[subdivision];
    }
}
