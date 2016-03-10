package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Filters the same proportion for each age and sex
 * Created by carrknight on 3/10/16.
 */
public class FixedProportionFilter implements AbundanceFilter
{




    final private double proportion;

    public FixedProportionFilter(double proportion) {
        Preconditions.checkArgument(proportion>=0, "Proportion filter cannot be negative");
        Preconditions.checkArgument(proportion<=1, "Proportion filter cannot be above 1");
        this.proportion = proportion;
    }

    /**
     * returns a int[2][age+1] array with male and female fish that are not filtered out
     *
     * @param male    the abundance array for male
     * @param female  the abundance array for female
     * @param species the species of fish
     * @return an int[2][age+1] array for all the stuff that is caught/selected and so on
     */
    @Override
    public int[][] filter(int[] male, int[] female, Species species)
    {
        int[][] filtered = new int[2][species.getMaxAge()+1];
        for(int age =0; age<species.getMaxAge()+1; age++)
        {
            filtered[FishStateUtilities.FEMALE][age] = (int)(female[age] * proportion + 0.5d);
            filtered[FishStateUtilities.MALE][age] = (int)(male[age] * proportion + 0.5d);
        }

        return filtered;
    }
}
