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
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Basically a transposition of the BiomassDiffuser to abundance
 * Created by carrknight on 7/7/17.
 */
public class ConstantRateAbundanceDiffuser extends AbstractAbundanceDiffuser {

    /**
     * % of differential that moves from here to there
     */
    private final double diffusingRate;


    public ConstantRateAbundanceDiffuser(
            int diffusingRange,
            double diffusingRate) {
        super(diffusingRange, true);
        Preconditions.checkArgument(diffusingRate >=0);
        Preconditions.checkArgument(diffusingRate <=1);
        this.diffusingRate = diffusingRate;
    }

    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     *  @param species      species moving!
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
            Species species, SeaTile here, AbundanceBasedLocalBiology biologyHere,
            SeaTile there, AbundanceBasedLocalBiology biologyThere, double delta, double fishHere, double fishThere, int bin,
            boolean male,
            MersenneTwisterFast random,
            boolean rounding)
    {
        if(delta<=0)
            return;

        double movement = delta * diffusingRate;
        if(rounding)
            movement = FishStateUtilities.randomRounding(delta * diffusingRate,
                                                         random);

        //might be too small differential for movement
        if(movement > 0)
        {

            //move!
            if(male) {
                biologyHere.getNumberOfMaleFishPerAge(species)[bin] -= movement;
                assert biologyHere.getNumberOfMaleFishPerAge(species)[bin] >= 0;
                biologyThere.getNumberOfMaleFishPerAge(species)[bin] += movement;
                assert biologyThere.getNumberOfMaleFishPerAge(species)[bin] >= 0;
            }
            else
            {
                biologyHere.getNumberOfFemaleFishPerAge(species)[bin] -= movement;
                assert biologyHere.getNumberOfFemaleFishPerAge(species)[bin] >= 0;
                biologyThere.getNumberOfFemaleFishPerAge(species)[bin] += movement;
                assert biologyThere.getNumberOfFemaleFishPerAge(species)[bin] >= 0;
            }
        }



    }
}
