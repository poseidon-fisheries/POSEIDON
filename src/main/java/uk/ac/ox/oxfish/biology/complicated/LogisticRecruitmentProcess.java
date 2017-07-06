package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.growers.IndependentLogisticBiomassGrower;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Logistic recruitment: weight all the biomass, take a logisitc step and transform it back into abundance
 * Created by carrknight on 7/5/17.
 */
public class LogisticRecruitmentProcess implements RecruitmentProcess {


    final private double carryingCapacity;

    final private double malthusianParameter;

    private NoiseMaker noise = new NoNoiseMaker();


    public LogisticRecruitmentProcess(double carryingCapacity,
                                      double malthusianParameter) {
        this.carryingCapacity = carryingCapacity;
        this.malthusianParameter = malthusianParameter;
    }

    /**
     * Computes the number of new recruits per sex
     *
     * @param species      the species of fish examined
     * @param meristics    the biological characteristics of the fish
     * @param femalePerAge the number of females that are part of the recruitment, grouped by age cohort
     * @param malePerAge   the number of males that are part of the recruitment, grouped by age cohort
     * @return the number of male + female recruits
     */
    @Override
    public int recruit(Species species, Meristics meristics, int[] femalePerAge, int[] malePerAge) {

        //weigh
        double biomass = FishStateUtilities.weigh(malePerAge,femalePerAge,meristics);

        double nextBiomass = IndependentLogisticBiomassGrower.logisticStep(
                biomass + noise.get(),carryingCapacity,malthusianParameter
        );

        double recruitmentBiomass = nextBiomass-biomass;
        double recruitAverageWeight = meristics.getWeightFemaleInKg().get(0) + meristics.getWeightMaleInKg().get(0);
        recruitAverageWeight/=2;

        assert  recruitmentBiomass >=0;

        //turn weight into # of recruits and return it!
        return recruitmentBiomass > 0 ? (int)(recruitmentBiomass/recruitAverageWeight) :0;
    }

    /**
     * give a function to generate noise as % of recruits this year
     *
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    @Override
    public void addNoise(NoiseMaker noiseMaker) {
            this.noise = noiseMaker;
    }
}
