package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

public class UnreliableFishValueCalculator implements FishValueCalculator {

    private final FishValueCalculator delegate;
    private final MersenneTwisterFast rng;
    private final double standardDeviation;

    public UnreliableFishValueCalculator(final Fisher fisher, final double standardDeviation) {
        this.delegate = new ReliableFishValueCalculator(fisher);
        this.rng = fisher.grabRandomizer();
        this.standardDeviation = standardDeviation;
    }

    private double addError(final double value) {
        return Math.max(0, value + value * rng.nextGaussian() * standardDeviation);
    }

    @Override
    public double valueOf(final Catch catchesKept) {
        return addError(delegate.valueOf(catchesKept));
    }

    @Override
    public double valueOf(final double[] biomass) {
        return addError(delegate.valueOf(biomass));
    }

    @Override
    public double valueOf(final LocalBiology biology) {
        return delegate.valueOf(biology);
    }
}
