package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A fixed proportion of fish moves from one bin to the next each step
 * Created by carrknight on 7/6/17.
 */
public class ProportionalAgingProcess implements AgingProcess {


    /**
     * generates a number between 0 and 1 (the method bounds it so otherwise) representing
     * how many fish of class x move between one bin and the next
     */
    private final DoubleParameter proportionAging;


    public ProportionalAgingProcess(DoubleParameter proportionAging) {
        this.proportionAging = proportionAging;
    }

    /**
     * as a side-effect ages the local biology according to its rules
     *
     * @param localBiology
     * @param species
     * @param model
     */
    @Override
    public void ageLocally(
            AbundanceBasedLocalBiology localBiology, Species species, FishState model)
    {


        //get the age structure (these are not copies!)
        int[] males = localBiology.getNumberOfMaleFishPerAge(species);
        int[] females = localBiology.getNumberOfFemaleFishPerAge(species);

        //go from oldest to youngest and age them (to avoid double aging)
        for(int bin=species.getMaxAge(); bin>=0; bin--)
        {
            //male
            int deltaMale = proportionalStep(males[bin],model.getRandom());
            males[bin]-=deltaMale;
            if(bin<species.getMaxAge()) //if you are at very last bin, you just die
                males[bin+1]+=deltaMale;
            //female
            int deltaFemale = proportionalStep(females[bin],model.getRandom());
            females[bin]-=deltaFemale;
            if(bin<species.getMaxAge()) //if you are at very last bin, you just die
                //otherwise you age one class
                females[bin+1]+=deltaFemale;
        }



    }


    /**
     * tells you for these many fish how many age and how many don't
     * @param binAbundance the number of fish
     * @return fish that move to the next bin
     */
    private int proportionalStep(int binAbundance, MersenneTwisterFast random)
    {

        Preconditions.checkArgument(binAbundance>=0);
        if(binAbundance == 0)
            return 0;
        double proportion = Math.max(0,Math.min(1,proportionAging.apply(random)));
        return (int) (proportion * binAbundance);

    }
}
