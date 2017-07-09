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
        super(diffusingRange);
        Preconditions.checkArgument(diffusingRate >=0);
        Preconditions.checkArgument(diffusingRate <=1);
        this.diffusingRate = diffusingRate;
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
     * @param bin          bin/age studied
     * @param male         whether it's male or female
     * @param random
     */
    @Override
    public void move(
            Species species, SeaTile here, AbundanceBasedLocalBiology biologyHere,
            SeaTile there, AbundanceBasedLocalBiology biologyThere, int delta, int bin, boolean male,
            MersenneTwisterFast random)
    {

        int movement = FishStateUtilities.randomRounding(delta * diffusingRate,
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
