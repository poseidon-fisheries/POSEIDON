package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;

public class UnreliableFishValueCalculator implements FishValueCalculator {

    private final FishValueCalculator delegate;
    private final MersenneTwisterFast rng;
    private final double standardDeviation;

    public UnreliableFishValueCalculator(
        final Fisher fisher,
        final double standardDeviation
    ) {
        this(fisher.grabState().getBiology(), fisher.grabRandomizer(), standardDeviation);
    }

    public UnreliableFishValueCalculator(
        final GlobalBiology globalBiology,
        final MersenneTwisterFast rng,
        final double standardDeviation
    ) {
        this.delegate = new ReliableFishValueCalculator(globalBiology);
        this.rng = rng;
        this.standardDeviation = standardDeviation;
    }

    private double addError(final double value) {
        return Math.max(0, value + value * rng.nextGaussian() * standardDeviation);
    }

    @Override
    public GlobalBiology getGlobalBiology() {
        return delegate.getGlobalBiology();
    }

    @Override
    public double valueOf(final double[] biomasses, final double[] prices) {
        return addError(delegate.valueOf(biomasses, prices));
    }

}
