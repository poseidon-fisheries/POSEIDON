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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * Simply provide arrays (one for each subdivision), each representing the age structure
 * Useful for things like Sablefish which have no set formula.
 * Created by carrknight on 3/21/17.
 */
public class ArrayFilter  implements AbundanceFilter{


    private final double filters[][];

    /**
     * do we round abundances so that only integer number of fish can be caught?
     */
    private final boolean round;


    public ArrayFilter(boolean round, double[]... filters) {
        this.filters = new double[filters.length][];
        for(int i=0; i< filters.length; i++)
            this.filters[i] = filters[i];
        this.round = round;
    }

    /**
     * returns a int[subdivisions][age+1] array with male and female fish that are not filtered out
     *
     * @param species the species of fish
     * @param abundance
     * @return an int[2][age+1] array for all the stuff that is caught/selected and so on
     */
    @Override
    public double[][] filter(Species species, double[][] abundance) {

        for(int subdivision =0; subdivision < abundance.length; subdivision++)
        {
            for (int age = 0; age < abundance[subdivision].length; age++) {
                abundance[subdivision][age] = (filters[subdivision][age] * abundance[subdivision][age]);
                if (round) {
                    abundance[subdivision][age] =FishStateUtilities.quickRounding(abundance[subdivision][age]);
                }

            }
        }
        return abundance;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayFilter that = (ArrayFilter) o;

        if (round != that.round) return false;
        return Arrays.deepEquals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        int result = Arrays.deepHashCode(filters);
        result = 31 * result + (round ? 1 : 0);
        return result;
    }
}
