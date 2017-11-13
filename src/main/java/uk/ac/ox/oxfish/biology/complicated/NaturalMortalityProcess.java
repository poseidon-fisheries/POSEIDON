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
public class NaturalMortalityProcess
{


    /**
     * as a side-effect modifies male and female cohorts by killing a % of its population equal to the mortality rate.
     * @param male array containing male fish per age
     * @param female array with female fish per age
     * @param species the characteristics of the species
     * @param rounding
     */
    public void cull(double[] male, double[] female, Meristics species, boolean rounding)
    {
        double maleMortality = species.getMortalityParameterMMale();
        double femaleMortality = species.getMortalityParameterMFemale();
        Preconditions.checkArgument(male.length==female.length);
        for(int i=0;i<male.length; i++)
        {
            male[i] = (male[i] * Math.exp(-maleMortality) );
            female[i] = (female[i] * Math.exp(-femaleMortality));
            if(rounding) {
                male[i] = (int) FishStateUtilities.round(male[i]);
                female[i] = (int) FishStateUtilities.round(female[i]);
            }
        }

    }


}
