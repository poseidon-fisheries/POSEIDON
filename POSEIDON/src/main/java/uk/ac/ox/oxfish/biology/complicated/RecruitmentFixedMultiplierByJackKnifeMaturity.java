package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

/**
 * follows a simple linear relationship between recruits and SSB a la 10.1007/s12562-017-1141-x
 */
public class RecruitmentFixedMultiplierByJackKnifeMaturity implements RecruitmentProcess {


    final private double lengthAtMaturity;


    private final Map<Integer, DoubleParameter> spawningDayToScaling;


    private final MersenneTwisterFast randomizer;
    /**
     * noisemaker is applied an additional shock to the MULTIPLIER
     */
    private NoiseMaker noiseMaker = new NoNoiseMaker();

    public RecruitmentFixedMultiplierByJackKnifeMaturity(
        final double lengthAtMaturity,
        final Map<Integer, DoubleParameter> spawningDayToScaling,
        final MersenneTwisterFast randomizer
    ) {
        this.lengthAtMaturity = lengthAtMaturity;
        this.spawningDayToScaling = spawningDayToScaling;
        this.randomizer = randomizer;
    }

    @Override
    public double recruit(
        final Species species,
        final Meristics meristics,
        final StructuredAbundance abundance,
        final int dayOfTheYear,
        final int daysSimulated
    ) {

        Preconditions.checkState(daysSimulated == 365, "Natural process needs to be daily for seasonality to work");

        if (spawningDayToScaling.containsKey(dayOfTheYear)) {
            final Double recruitPerSSB = spawningDayToScaling.get(dayOfTheYear)
                .applyAsDouble(randomizer) * (1 + noiseMaker.get());
            return recruitPerSSB * getSSB(species, abundance);

        } else
            return 0;


    }

    private double getSSB(final Species species, final StructuredAbundance abundance) {

        Preconditions.checkArgument(
            abundance.getSubdivisions() == 1,
            "I am assuming this is a simple boxcar or anyway no split between male/female"
        );
        double ssb = 0;
        for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
            if (species.getLength(0, bin) >= lengthAtMaturity)
                ssb += species.getWeight(0, bin) * abundance.getAbundanceInBin(bin);

        }
        return ssb;


    }

    @Override
    public void addNoise(final NoiseMaker noiseMaker) {

        this.noiseMaker = noiseMaker;

    }
}
