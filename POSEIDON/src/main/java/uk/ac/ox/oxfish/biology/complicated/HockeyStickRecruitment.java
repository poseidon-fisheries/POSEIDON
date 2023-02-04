package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;

/**
 * Barrowman (2000) recruitments are R0 whenever Depletion > hinge. Otherwise recruits are R0* depletion/hinge
 */
public class HockeyStickRecruitment extends YearlyRecruitmentProcess {


    private final double hinge;

    final private double virginRecruits;

    final private double lengthAtMaturity;

    final private double virginSpawningBiomass;

    public HockeyStickRecruitment(boolean recruitEveryday,
                                  double hinge,
                                  double virginRecruits,
                                  double lengthAtMaturity,
                                  double virginSpawningBiomass) {
        super(recruitEveryday);
        this.hinge = hinge;
        this.virginRecruits = virginRecruits;
        this.lengthAtMaturity = lengthAtMaturity;
        this.virginSpawningBiomass = virginSpawningBiomass;
    }

    private NoiseMaker noiseMaker = new NoNoiseMaker();



    @Override
    protected double computeYearlyRecruitment(Species species, Meristics meristics, StructuredAbundance abundance) {
        double depletion = LinearSSBRatioSpawning.
                computeDepletion(species, meristics, abundance, lengthAtMaturity, virginSpawningBiomass);

        return Math.min(1d,depletion/hinge) * virginRecruits * (1+noiseMaker.get());
    }

    @Override
    public void addNoise(NoiseMaker noiseMaker) {
        this.noiseMaker = noiseMaker;
    }

    public double getHinge() {
        return hinge;
    }

    public double getVirginRecruits() {
        return virginRecruits;
    }

    public double getLengthAtMaturity() {
        return lengthAtMaturity;
    }

    public double getVirginSpawningBiomass() {
        return virginSpawningBiomass;
    }
}
