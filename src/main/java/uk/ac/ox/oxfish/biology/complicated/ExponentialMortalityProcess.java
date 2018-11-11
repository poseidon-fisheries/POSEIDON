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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Simply culls a % of fish each year according to their mortality rate
 * Created by carrknight on 3/2/16.
 */
public class ExponentialMortalityProcess implements NaturalMortalityProcess {


    private final double[] mortalityParameters;

    public ExponentialMortalityProcess(double... mortalityParameters) {
        this.mortalityParameters = mortalityParameters;
    }

    /**
     * the california input
     */
    public ExponentialMortalityProcess(StockAssessmentCaliforniaMeristics input) {
        mortalityParameters = new double[2];
        mortalityParameters[FishStateUtilities.MALE] = input.getMortalityParameterMMale();
        mortalityParameters[FishStateUtilities.FEMALE] = input.getMortalityParameterMFemale();

    }

    /**
     * as a side-effect modifies male and female cohorts by killing a % of its population equal to the mortality rate.
     * @param species the characteristics of the species
     * @param rounding
     * @param structuredAbundance
     * @param daysSimulated
     */
    @Override
    public void cull(
            Meristics species,
            boolean rounding,
            StructuredAbundance structuredAbundance,
            int daysSimulated)
    {
        assert species.getNumberOfSubdivisions() == structuredAbundance.getSubdivisions();
        Preconditions.checkArgument(species.getNumberOfSubdivisions() ==mortalityParameters.length,
                                    "There ought to be a mortality parameter for each subdivision/cohort");


        double scaling = daysSimulated/365d;
        double[][] abundance = structuredAbundance.asMatrix();
        for(int subdivision=0; subdivision<species.getNumberOfSubdivisions(); subdivision++)
        {
            for (int i = 0; i < structuredAbundance.getBins(); i++)
            {

                abundance[subdivision][i] =  abundance[subdivision][i] *
                        Math.exp((-mortalityParameters[subdivision]*scaling));
                if (rounding) {
                    abundance[subdivision][i] = (int) FishStateUtilities.round(abundance[subdivision][i]);
                }
            }
        }
    }


}
