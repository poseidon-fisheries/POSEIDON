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
     *
     * @param species
     * @param here         departing point
     * @param biologyHere  departing local biology
     * @param there        arriving point
     * @param biologyThere arriving local biology
     * @param delta        number of fish here - number of fish there (always positive or this isn't called)
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
            int delta, int bin, boolean male,
            MersenneTwisterFast random) {

        if(bin >= minMovementAge && bin<=maxMovementAge)
            super.move(species, here, biologyHere, there, biologyThere, delta, bin, male, random);
    }
}
