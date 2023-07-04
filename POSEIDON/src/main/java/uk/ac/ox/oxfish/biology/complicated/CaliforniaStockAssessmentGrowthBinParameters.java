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

import static java.lang.Math.max;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.MALE;

/**
 * follows the california stock assessment formulas where bins are age classes, and the subdivision is MALE-FEMALE
 */
public class CaliforniaStockAssessmentGrowthBinParameters implements Meristics {


    private final int maxAge;


    private final double[][] weights;

    private final double[][] lengths;


    /**
     * creates what is basically Von Bertalanffy but with very particular parametrization
     *
     * @param maxAge                 max age (which is the last bin)
     * @param youngLengthMale        length at "ageYoung"
     * @param oldLengthMale          length at "ageOld"
     * @param weightParameterAMale   alpha transforming length to weight
     * @param weightParameterBMale   beta transforming length to weight
     * @param KParameterMale         growth parameter
     * @param youngLengthFemale      length at "ageYoung"
     * @param oldLengthFemale        length at "ageOld"
     * @param weightParameterAFemale alpha transforming length to weight
     * @param weightParameterBFemale beta transforming length to weight
     * @param KParameterFemale       the growth parameter (annual)
     * @param ageOld                 what year the fish is considered old
     * @param ageYoungMale           what year the male fish is considered young
     */
    public CaliforniaStockAssessmentGrowthBinParameters(
        final int maxAge, final double youngLengthMale,
        final double oldLengthMale,
        final double weightParameterAMale,
        final double weightParameterBMale,
        final double KParameterMale,
        final double youngLengthFemale,
        final double oldLengthFemale,
        final double weightParameterAFemale,
        final double weightParameterBFemale,
        final double KParameterFemale,
        final double ageOld,
        final double ageYoungMale,
        final double ageYoungFemale
    ) {


        this.maxAge = maxAge;
        //compute L-Inf for Von Bertalanffy
        final double LInfFemale =
            youngLengthFemale < ageOld
                ?
                youngLengthFemale + ((oldLengthFemale - youngLengthFemale) /
                    (1 - Math.exp(-KParameterFemale * (ageOld - ageYoungFemale))))
                :
                oldLengthFemale;
        final double LInfMale =
            youngLengthMale < ageOld
                ?
                youngLengthMale + ((oldLengthMale - youngLengthMale) /
                    (1 - Math.exp(-KParameterMale * (ageOld - ageYoungMale))))
                :
                oldLengthMale;

        //set up the the weights and lengths containers
        weights = new double[2][];
        weights[0] = new double[maxAge + 1];
        weights[1] = new double[maxAge + 1];
        lengths = new double[2][];
        lengths[0] = new double[maxAge + 1];
        lengths[1] = new double[maxAge + 1];

        //compute each year Bertalanffy length
        for (int age = 0; age < maxAge + 1; age++) {
            lengths[FEMALE][age] = LInfFemale + ((youngLengthFemale - LInfFemale)) *
                Math.exp(-KParameterFemale * (age - ageYoungFemale));
            //the formulas lead to negative lenghts for very small fish, here we just round it to 0
            if (lengths[FEMALE][age] < 0)
                lengths[FEMALE][age] = 0d;
            weights[FEMALE][age] = weightParameterAFemale * Math.pow(
                lengths[FEMALE][age],
                weightParameterBFemale
            );


            lengths[MALE][age] = LInfMale + ((youngLengthMale - LInfMale)) *
                Math.exp(-KParameterMale * (age - ageYoungMale));
            if (lengths[MALE][age] < 0)
                lengths[MALE][age] = 0d;
            weights[MALE][age] = weightParameterAMale * Math.pow(lengths[MALE][age], weightParameterBMale);
        }


    }

    @Override
    public double getWeight(final int subdivision, final int bin) {

        return weights[subdivision][bin];
    }

    /**
     * male-female
     *
     * @return
     */
    @Override
    public int getNumberOfSubdivisions() {
        return 2;
    }

    /**
     * number of bins for each subdivision. these are just age-classes here
     *
     * @return
     */
    @Override
    public int getNumberOfBins() {
        return maxAge + 1;
    }

    /**
     * because bins represent age here, this is just a lookup; we always round down the age
     *
     * @param ageInYears  age in terms of years
     * @param subdivision the subdivision we are study (male/female is different for example)
     * @return the length of the fish
     */
    @Override
    public double getLengthAtAge(final int ageInYears, final int subdivision) {
        return getLength(subdivision, max(ageInYears, maxAge));
    }

    @Override
    public double getLength(final int subdivision, final int bin) {
        return lengths[subdivision][bin];
    }
}
