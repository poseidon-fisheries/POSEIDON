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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * like constant rate abundance diffuser, but only works for a certain age range (outside this range fish is immobile!)
 *
 * Created by carrknight on 7/7/17.
 */
public class AgeLimitedConstantRateDiffuser extends ConstantRateAbundanceDiffuser {


    /**
     * fish of this age or above can move
     */
    private final int minMovementAge;

    /**
     * fish of this age or below can move
     */
    private  final  int maxMovementAge;


    public AgeLimitedConstantRateDiffuser(
            int diffusingRange, double diffusingRate,
            int minMovementAge, int maxMovementAge) {
        super(diffusingRange, diffusingRate);
        this.minMovementAge = minMovementAge;
        this.maxMovementAge = maxMovementAge;
    }

    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     *  @param species
     * @param here         departing point
     * @param biologyHere  departing local biology
     * @param there        arriving point
     * @param biologyThere arriving local biology
     * @param delta        number of fish here - number of fish there (always positive or this isn't called)
     * @param fishHere
     * @param fishThere
     * @param bin          bin/age studied
     * @param male         whether it's male or female
     * @param random
     */
    @Override
    public void move(
            Species species,
            SeaTile here, AbundanceBasedLocalBiology biologyHere,
            SeaTile there,
            AbundanceBasedLocalBiology biologyThere,
            int delta, int fishHere, int fishThere, int bin, boolean male,
            MersenneTwisterFast random) {

        if(delta<=0)
            return;
        if(bin >= minMovementAge && bin<=maxMovementAge)
            super.move(species, here, biologyHere, there, biologyThere, delta, fishHere, fishThere, bin, male, random);
    }
}
