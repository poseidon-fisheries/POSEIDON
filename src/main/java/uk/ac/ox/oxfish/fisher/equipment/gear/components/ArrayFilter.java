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
 * Simply provide two arrays (one for male, one for female), each representing the age structure
 * Useful for things like Sablefish which have no set formula.
 * Created by carrknight on 3/21/17.
 */
public class ArrayFilter  implements AbundanceFilter{


    private final double maleFilter[];

    private final double femaleFilter[];

    /**
     * do we round abundances so that only integer number of fish can be caught?
     */
    private final boolean round;


    public ArrayFilter(double[] maleFilter, double[] femaleFilter, boolean round) {
        this.maleFilter = maleFilter;
        this.femaleFilter = femaleFilter;
        this.round = round;
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
    public double[][] filter(double[] male, double[] female, Species species) {
        Preconditions.checkArgument(maleFilter.length == male.length);
        Preconditions.checkArgument(femaleFilter.length == female.length);
        Preconditions.checkArgument(male.length == female.length);
        double[][] filtered = new double[2][male.length];
        for(int age =0; age < male.length; age++)
        {
            filtered[FishStateUtilities.MALE][age] = (maleFilter[age] * male[age]);
            filtered[FishStateUtilities.FEMALE][age] = (femaleFilter[age] * female[age] );
            if(round) {
                filtered[FishStateUtilities.FEMALE][age] = (int) (filtered[FishStateUtilities.FEMALE][age] + 0.5d);
                filtered[FishStateUtilities.MALE][age] = (int)(filtered[FishStateUtilities.MALE][age]  + 0.5d );
            }

        }

        return filtered;
    }
}
