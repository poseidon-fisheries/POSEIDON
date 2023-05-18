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

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableDoubleArray;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Simply provide arrays (one for each subdivision), each representing the age structure
 * Useful for things like Sablefish which have no set formula.
 * Created by carrknight on 3/21/17.
 */
public class ArrayFilter implements AbundanceFilter {


    private final double[][] filters;

    /**
     * do we round abundances so that only integer number of fish can be caught?
     */
    private final boolean round;


    public ArrayFilter(final boolean round, final double[]... filters) {
        this.filters = new double[filters.length][];
        System.arraycopy(filters, 0, this.filters, 0, filters.length);
        this.round = round;
    }


    public static ArrayFilter nonMutatingArrayFilter(final Collection<Collection<Double>> filters) {
        final double[][] filterArray = convertCollectionToPOJOArray(filters);
        return new ArrayFilter(false, filterArray);
    }

    protected static double[][] convertCollectionToPOJOArray(final Collection<Collection<Double>> filters) {
        final ImmutableList<ImmutableDoubleArray> collected = filters.stream()
            .map(ImmutableDoubleArray::copyOf)
            .collect(toImmutableList());
        final double[][] filterArray = new double[collected.size()][collected.get(0).length()];
        for (int row = 0; row < collected.size(); row++) {
            for (int bin = 0; bin < collected.get(0).length(); bin++) {
                filterArray[row][bin] = collected.get(row).get(bin);
            }
        }
        return filterArray;
    }


    /**
     * returns a int[subdivisions][age+1] array with male and female fish that are not filtered out
     *
     * @param species   the species of fish
     * @param abundance
     * @return an int[2][age+1] array for all the stuff that is caught/selected and so on
     */
    @Override
    public double[][] filter(final Species species, final double[][] abundance) {

        for (int subdivision = 0; subdivision < abundance.length; subdivision++) {
            for (int age = 0; age < abundance[subdivision].length; age++) {
                abundance[subdivision][age] = (filters[subdivision][age] * abundance[subdivision][age]);
                if (round) {
                    abundance[subdivision][age] = FishStateUtilities.quickRounding(abundance[subdivision][age]);
                }

            }
        }
        return abundance;
    }

    public double getFilterValue(final int subdivision, final int bin) {
        return filters[subdivision][bin];
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ArrayFilter that = (ArrayFilter) o;

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
