package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

/**
 * follows a simple linear relationship between recruits and SSB a la 10.1007/s12562-017-1141-x
 */
public class RecruitmentFixedMultiplierByJackKnifeMaturity implements  RecruitmentProcess {



    final private double lengthAtMaturity;




    private final Map<Integer,DoubleParameter> spawningDayToScaling;


    private final MersenneTwisterFast randomizer;


    public RecruitmentFixedMultiplierByJackKnifeMaturity(double lengthAtMaturity,
                                                         Map<Integer, DoubleParameter> spawningDayToScaling,
                                                         MersenneTwisterFast randomizer) {
        this.lengthAtMaturity = lengthAtMaturity;
        this.spawningDayToScaling = spawningDayToScaling;
        this.randomizer = randomizer;
    }

    /**
     * noisemaker is applied an additional shock to the MULTIPLIER
     */
    private NoiseMaker noiseMaker = new NoNoiseMaker();

    @Override
    public double recruit(Species species, Meristics meristics, StructuredAbundance abundance, int dayOfTheYear, int daysSimulated) {

        Preconditions.checkState(daysSimulated==365, "Natural process needs to be daily for seasonality to work");

        if(spawningDayToScaling.containsKey(dayOfTheYear))
        {
            final Double recruitPerSSB = spawningDayToScaling.get(dayOfTheYear).apply(randomizer) *(1+noiseMaker.get());
            return recruitPerSSB * getSSB(species,abundance) ;

        }
        else
            return 0;




    }

    @Override
    public void addNoise(NoiseMaker noiseMaker) {

        this.noiseMaker = noiseMaker;

    }


    private double getSSB(Species species, StructuredAbundance abundance){

        Preconditions.checkArgument(abundance.getSubdivisions()==1,"I am assuming this is a simple boxcar or anyway no split between male/female");
        double ssb = 0;
        for(int bin=0; bin<species.getNumberOfBins(); bin++) {
            if(species.getLength(0, bin) >= lengthAtMaturity )
                ssb += species.getWeight(0,bin) * abundance.getAbundanceInBin(bin);

        }
        return ssb;



    }
}
