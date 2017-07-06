package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Simple aging structure where each cohort is aged by 1;
 * Basically this makes sense if the abundance is split really into ages rather than
 * other forms of bins
 * Created by carrknight on 7/6/17.
 */
public class StandardAgingProcess implements AgingProcess {


    public StandardAgingProcess(boolean preserveLastAge) {
        this.preserveLastAge = preserveLastAge;
    }

    /**
     * if this is false, last year fish dies off. Otherwise it accumulates in the last bin
     */
    final boolean preserveLastAge;

    /**
     * as a side-effect ages the local biology according to its rules
     *
     * @param localBiology
     * @param model
     */
    @Override
    public void ageLocally(AbundanceBasedLocalBiology localBiology,Species species,
                           FishState model)
    {

        //get the age structure (these are not copies!)
        int[] males = localBiology.getNumberOfMaleFishPerAge(species);
        int[] females = localBiology.getNumberOfFemaleFishPerAge(species);

        //store these in case you need to preserve last age
        int oldestMale = males[males.length-1];
        int oldestFemale = females[females.length-1];

        System.arraycopy(males,0,males,1,males.length-1);
        System.arraycopy(females,0,females,1,females.length-1);
        males[0] = 0;
        females[0] = 0;
        if(preserveLastAge)
        {
            males[males.length - 1] += oldestMale;
            females[females.length - 1] += oldestFemale;

        }
    }


    /**
     * Getter for property 'preserveLastAge'.
     *
     * @return Value for property 'preserveLastAge'.
     */
    public boolean isPreserveLastAge() {
        return preserveLastAge;
    }
}
