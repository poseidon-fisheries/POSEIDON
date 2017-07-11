package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Like its parent class but delays the recruitment by a number of years
 * Created by carrknight on 3/7/16.
 */
public class RecruitmentBySpawningBiomassDelayed extends RecruitmentBySpawningBiomass {

    private int yearDelay;

    private final Queue<Integer> recruits = new LinkedList<>();

    public RecruitmentBySpawningBiomassDelayed(
            int virginRecruits, double steepness,
            double cumulativePhi, boolean addRelativeFecundityToSpawningBiomass, int yearDelay) {
        super(virginRecruits, steepness, cumulativePhi, addRelativeFecundityToSpawningBiomass);
        this.yearDelay = yearDelay;
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
    public int recruit(Species species, Meristics meristics, int[] femalePerAge, int[] malePerAge)
    {

        int newRecruit = super.recruit(species, meristics, femalePerAge, malePerAge);
        if(recruits.isEmpty())
            initializeQueue(newRecruit);
        assert recruits.size() == yearDelay;

        recruits.add(newRecruit);
        return recruits.poll();
    }

    public void initializeQueue(int newRecruit) {
        while (recruits.size() < yearDelay)
            recruits.add(newRecruit);
    }
}
