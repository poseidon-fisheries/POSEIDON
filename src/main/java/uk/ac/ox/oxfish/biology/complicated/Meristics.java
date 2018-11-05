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

/**
 * this interface keeps track of basically how much a fish in a bin weigh and is long as well as how many bins and
 * subdivisions there actually are
 *
 */
public interface Meristics {

    double getLength(int subdivision, int bin);

    /**
     * get the weight of one fish
     * @param subdivision at this division
     * @param bin at this bin
     * @return the unit weight
     */
    double getWeight(int subdivision, int bin);

    /**
     * subdivision are groups like male-female or age cohorts
     * @return
     */
    int getNumberOfSubdivisions();

    /**
     * number of bins for each subdivision. All subdivisions are assumed to have these number of bins
     * and all bins with the same index refer to the same weight and length; <br>
     *     Bins can be length-bins or age-bins, it depends on the use case
     * @return
     */
    int getNumberOfBins();


    /**
     * function mapping time to length; the growth function.
     * It doesn't have to be consistent with the subdivisions but it should
     * @param ageInYears age in terms of years
     * @param subdivision the subdivision we are study (male/female is different for example)
     * @return the length of the fish
     */
    double getLengthAtAge(int ageInYears, int subdivision);

}
