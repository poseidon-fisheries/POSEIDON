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




    private final HashMap<AbundanceBasedLocalBiology,Double> weights;


    public WeightedAbundanceDiffuser(
            int diffusingRange, double diffusingRate,
            HashMap<AbundanceBasedLocalBiology, Double> weights) {
        super(diffusingRange, diffusingRate);
        this.weights = weights;
    }


    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     *
     * @param species      species moving!
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
            Species species, SeaTile here, AbundanceBasedLocalBiology biologyHere, SeaTile there,
            AbundanceBasedLocalBiology biologyThere, int delta, int fishHere, int fishThere, int bin, boolean male,
            MersenneTwisterFast random) {



        //reweights
        double weightHere = weights.get(biologyHere);
        double weightThere = weights.get(biologyThere);
        delta = (int) ((fishHere * weightThere  - fishThere * weightHere)/(weightHere+weightThere));
        super.move(species, here, biologyHere, there, biologyThere, delta, fishHere, fishThere, bin, male, random);


    }
}