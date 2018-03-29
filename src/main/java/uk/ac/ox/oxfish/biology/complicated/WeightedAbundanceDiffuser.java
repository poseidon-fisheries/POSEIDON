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

import java.util.HashMap;

/**
 * Given a set of weights to drive movement towards better habitat
 * Created by carrknight on 7/28/17.
 */
public class WeightedAbundanceDiffuser extends ConstantRateAbundanceDiffuser{




    private final HashMap<AbundanceLocalBiology,Double> weights;


    public WeightedAbundanceDiffuser(
            int diffusingRange, double diffusingRate,
            HashMap<AbundanceLocalBiology, Double> weights) {
        super(diffusingRange, diffusingRate);
        this.weights = weights;
    }


    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     *
     * @param species      species moving!
     * @param here         departing point
     * @param abundanceHere  departing local biology
     * @param there        arriving point
     * @param abundanceThere arriving local biology
     * @param delta        number of fish here - number of fish there (always positive or this isn't called)
     * @param fishHere
     * @param fishThere
     * @param bin          bin/age studied
     * @param random
     * @param subdivision
     * @param biologyHere  departing local biology
     * @param biologyThere arriving local biology
     */
    @Override
    public void move(
            Species species, SeaTile here, StructuredAbundance abundanceHere, SeaTile there,
            StructuredAbundance abundanceThere, double delta, double fishHere, double fishThere, int bin,
            MersenneTwisterFast random,
            boolean rounding, int subdivision,
            AbundanceLocalBiology biologyHere,
            AbundanceLocalBiology biologyThere) {



        //reweights
        double weightHere = weights.get(biologyHere);
        double weightThere = weights.get(biologyThere);
        delta = ((fishHere * weightThere  - fishThere * weightHere)/(weightHere+weightThere));
        if(rounding)
            delta=(int) delta;

        super.move(species, here, abundanceHere, there, abundanceThere, delta, fishHere, fishThere, bin, random, rounding,
                   subdivision, biologyHere, biologyThere);


    }
}