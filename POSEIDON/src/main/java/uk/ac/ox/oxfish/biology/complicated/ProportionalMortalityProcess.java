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

import uk.ac.ox.oxfish.utility.FishStateUtilities;

public class ProportionalMortalityProcess implements NaturalMortalityProcess {


    private final double yearlyMortality;

    public ProportionalMortalityProcess(double yearlyMortality) {
        this.yearlyMortality = yearlyMortality;
    }

    /**
     * calls same proportion of fish in each bin each step
     *
     * @param species             the fish species to kill
     * @param rounding            whether we want the process to round all abundances to closest int
     * @param structuredAbundance the current abundance of the fish (and what actually gets modified)
     * @param daysSimulated       how many days are we simulating the mortality for
     */
    @Override
    public void cull(
            Meristics species, boolean rounding, StructuredAbundance structuredAbundance, int daysSimulated)
    {
        double scaling = daysSimulated/365d;
        double effectiveMortality = 1d-Math.pow(1d-yearlyMortality,scaling);
        double[][] abundanceMatrix = structuredAbundance.asMatrix();


        for(int cohort=0; cohort<abundanceMatrix.length; cohort++)
            for(int bin=0; bin<abundanceMatrix[cohort].length; bin++) {
                abundanceMatrix[cohort][bin] -= abundanceMatrix[cohort][bin] * (effectiveMortality);
                if(rounding)
                    abundanceMatrix[cohort][bin] = FishStateUtilities.quickRounding(abundanceMatrix[cohort][bin]);
            }
    }
}
