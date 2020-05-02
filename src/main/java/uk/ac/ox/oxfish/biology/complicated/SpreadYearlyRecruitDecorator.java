package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.LinkedHashMap;

import static uk.ac.ox.oxfish.biology.complicated.YearlyRecruitmentProcess.YEARLY_RECRUITMENT_SPAWNING_DAY;

/**
 * takes a yearly recruitment object but calls it on specific days and shocks its
 * result to add noise
 */
public class SpreadYearlyRecruitDecorator implements RecruitmentProcess {




    /**
     * imagine this needs to spawn twice a year at day 100 and day 200, this would be:
     * 100 -- fixedDoubleParameter 0.5
     * 200 -- fixedDoubleParmater 0.5
     */
    private final LinkedHashMap<Integer, DoubleParameter> spawningDayToProportion;

    /**
     * the formula that actually computes yearly recruits
     */
    private final YearlyRecruitmentProcess delegate;


    private final MersenneTwisterFast random;


    public SpreadYearlyRecruitDecorator(LinkedHashMap<Integer, DoubleParameter> spawningDayToProportion,
                                        YearlyRecruitmentProcess delegate, MersenneTwisterFast random) {
        this.spawningDayToProportion = spawningDayToProportion;
        this.delegate = delegate;
        this.random = random;
        Preconditions.checkArgument(!delegate.isRecruitEveryday(),
                "This decorator assumes the formula is not already divided by 365");
        //todo this wouldn't be too difficult to fix. just multiply by 365/daysSimulated

    }


    @Override
    public double recruit(Species species, Meristics meristics,
                          StructuredAbundance abundance,
                          int dayOfTheYear,
                          int daysSimulated) {
        Preconditions.checkArgument(daysSimulated==1);

        //not a spawning day
        if(!spawningDayToProportion.containsKey(dayOfTheYear))
            return 0d;
        else{
            //spawning day
            final DoubleParameter scaling = spawningDayToProportion.get(dayOfTheYear);


            return scaling.apply(random) * delegate.recruit(
                    species, meristics, abundance,
                    YEARLY_RECRUITMENT_SPAWNING_DAY,
                    daysSimulated);

        }

    }


    public MersenneTwisterFast getRandom() {
        return random;
    }

    @Override
    public void addNoise(NoiseMaker noiseMaker) {
        delegate.addNoise(noiseMaker);
    }

    public LinkedHashMap<Integer, DoubleParameter> getSpawningDayToProportion() {
        return spawningDayToProportion;
    }

    public YearlyRecruitmentProcess getDelegate() {
        return delegate;
    }
}
