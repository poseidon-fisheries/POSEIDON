package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Supplier;
import uk.ac.ox.oxfish.biology.Species;

/**
 * A process to decide how many new fish of each sex are generated this year
 * Created by carrknight on 3/1/16.
 */
public interface RecruitmentProcess
{


    /**
     * Computes the number of new recruits per sex
     * @param species the species of fish examined
     * @param meristics the biological characteristics of the fish
     * @param femalePerAge the number of females that are part of the recruitment, grouped by age cohort
     * @param malePerAge the number of males that are part of the recruitment, grouped by age cohort
     * @return the number of male + female recruits
     */
    public int recruit(Species species,
                                 Meristics meristics,
                                 int[] femalePerAge,
                                 int[] malePerAge);


    /**
     * give a function to generate noise as % of recruits this year
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    void addNoise(Supplier<Double> noiseMaker);



}
