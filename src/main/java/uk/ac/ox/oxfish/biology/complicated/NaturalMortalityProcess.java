package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Simply culls a % of fish each year according to their mortality rate
 * Created by carrknight on 3/2/16.
 */
public class NaturalMortalityProcess
{


    /**
     * as a side-effect modifies male and female cohorts by killing a % of its population equal to the mortality rate.
     * @param male array containing male fish per age
     * @param female array with female fish per age
     * @param species the characteristics of the species
     */
    public void cull(int[] male,int[] female, Meristics species)
    {
        double maleMortality = species.getMortalityParameterMMale();
        double femaleMortality = species.getMortalityParameterMFemale();
        Preconditions.checkArgument(male.length==female.length);
        for(int i=0;i<male.length; i++)
        {
            male[i] = (int) FishStateUtilities.round(male[i] * Math.exp(-maleMortality) );
            female[i] = (int) FishStateUtilities.round(female[i] * Math.exp(-femaleMortality));
        }

    }


}
