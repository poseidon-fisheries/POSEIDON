package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Map;

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
            Species species,
            Map<SeaTile, AbundanceBasedLocalBiology> biologies, int diffusingRange,
            double diffusingRate) {
        super(species, biologies, diffusingRange);
        Preconditions.checkArgument(diffusingRate >=0);
        Preconditions.checkArgument(diffusingRate <=1);
        this.diffusingRate = diffusingRate;
    }

    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     *
     * @param random
     * @param here         departing point
     * @param biologyHere  departing local biology
     * @param there        arriving point
     * @param biologyThere arriving local biology
     * @param delta        number of fish here - number of fish there (always positive or this isn't called)
     * @param bin          bin/age studied
     * @param male         whether it's male or female
     */
    @Override
    public void move(
            MersenneTwisterFast random, SeaTile here, AbundanceBasedLocalBiology biologyHere, SeaTile there,
            AbundanceBasedLocalBiology biologyThere, int delta, int bin, boolean male)
    {

        int movement = FishStateUtilities.randomRounding(delta * diffusingRate,
                                                         random);
        //might be too small differential for movement
        if(movement > 0)
        {

            //move!
            if(male) {
                biologyHere.getNumberOfMaleFishPerAge(getSpecies())[bin] -= movement;
                assert biologyHere.getNumberOfMaleFishPerAge(getSpecies())[bin] >= 0;
                biologyThere.getNumberOfMaleFishPerAge(getSpecies())[bin] += movement;
                assert biologyThere.getNumberOfMaleFishPerAge(getSpecies())[bin] >= 0;
            }
            else
            {
                biologyHere.getNumberOfFemaleFishPerAge(getSpecies())[bin] -= movement;
                assert biologyHere.getNumberOfFemaleFishPerAge(getSpecies())[bin] >= 0;
                biologyThere.getNumberOfFemaleFishPerAge(getSpecies())[bin] += movement;
                assert biologyThere.getNumberOfFemaleFishPerAge(getSpecies())[bin] >= 0;
            }
        }



    }
}
