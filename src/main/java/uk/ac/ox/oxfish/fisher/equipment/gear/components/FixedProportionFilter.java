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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Filters the same proportion for each age and sex
 * Created by carrknight on 3/10/16.
 */
public class FixedProportionFilter implements AbundanceFilter
{




    final private double proportion;

    /**
     * should we round numbers so that the catches are always integers?
     */
    final private boolean rounding;

    public FixedProportionFilter(double proportion, boolean rounding) {
        this.rounding = rounding;
        Preconditions.checkArgument(proportion>=0, "Proportion filter cannot be negative");
        Preconditions.checkArgument(proportion<=1, "Proportion filter cannot be above 1");
        this.proportion = proportion;
    }

    /**
     * returns a int[2][age+1] array with male and female fish that are not filtered out
     *
     * @param male    the abundance array for male
     * @param female  the abundance array for female
     * @param species the species of fish
     * @return an int[2][age+1] array for all the stuff that is caught/selected and so on
     */
    @Override
    public double[][] filter(double[] male, double[] female, Species species)
    {
        double[][] filtered = new double[2][species.getMaxAge()+1];
        for(int age =0; age<species.getMaxAge()+1; age++)
        {
            filtered[FishStateUtilities.FEMALE][age] = (female[age] * proportion);
            filtered[FishStateUtilities.MALE][age] = (male[age] * proportion );

            if(rounding)
            {
                filtered[FishStateUtilities.FEMALE][age] = (int)(filtered[FishStateUtilities.FEMALE][age] + 0.5d);
                filtered[FishStateUtilities.MALE][age] = (int)(filtered[FishStateUtilities.MALE][age] + 0.5d);
            }
        }

        return filtered;
    }
}
