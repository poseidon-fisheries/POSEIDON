package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;

/**
 * Created by carrknight on 3/1/16.
 */
public class RecruitmentBySpawningBiomass implements RecruitmentProcess {


    /**
     * the number of recruits you'd get in a "virgin" state.
     */
    private final int virginRecruits;

    /**
     * logistic growth parameter
     */
    private final double steepness;

    /**
     * if true the spawning biomass counts relative fecundity (this is true for yelloweye rockfish)
     */
    private final boolean addRelativeFecundityToSpawningBiomass;


    public RecruitmentBySpawningBiomass(int virginRecruits,
                                        double steepness,
                                        boolean addRelativeFecundityToSpawningBiomass) {
        this.virginRecruits = virginRecruits;
        this.steepness = steepness;
        this.addRelativeFecundityToSpawningBiomass = addRelativeFecundityToSpawningBiomass;
    }

    /**
     * go through all females
     *
     * @param species      the species of fish examined
     * @param meristics    the biological characteristics of the fish
     * @param femalePerAge the number of females that are part of the recruitment, grouped by age cohort
     * @param malePerAge   the number of males that are part of the recruitment, grouped by age cohort
     * @return the number of male and female recruits
     */
    @Override
    public int recruit(
            Species species, Meristics meristics, int[] femalePerAge, int[] malePerAge)
    {

        //you need to sum up the spawning biomass of the fish:
        int cohorts = meristics.getMaxAgeFemale() + 1;
        Preconditions.checkArgument(femalePerAge.length == cohorts,
                                    "The number of cohorts is not equal to maxAge + 1");
        double spawningBiomass = 0;
        //compute the cumulative spawning biomass
        for(int i=0; i< cohorts; i++)
        {
            if(meristics.getWeightFemaleInKg()[i] > 0)
                if(!addRelativeFecundityToSpawningBiomass)
                    spawningBiomass += meristics.getWeightFemaleInKg()[i] * meristics.getMaturity()[i] * femalePerAge[i];
                else
                    spawningBiomass += meristics.getWeightFemaleInKg()[i] * meristics.getMaturity()[i] * femalePerAge[i];

        }

        //turn it into recruits.
        return
                (int) (
                        (4 * steepness * virginRecruits * spawningBiomass)/
                        ((virginRecruits*meristics.getCumulativePhi()*(1-steepness)) +
                                (((5*steepness)-1)*spawningBiomass))
                );



    }
}
