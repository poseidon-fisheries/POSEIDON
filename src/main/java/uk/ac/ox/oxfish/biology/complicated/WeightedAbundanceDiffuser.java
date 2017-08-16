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