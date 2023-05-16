package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import ec.util.MersenneTwisterFast;

import java.util.function.DoubleUnaryOperator;

public class LogNormalErrorOperator implements DoubleUnaryOperator {

    private final MersenneTwisterFast rng;
    private final double mean;
    private final double standardDeviation;

    public LogNormalErrorOperator(final MersenneTwisterFast rng, final double mean, final double standardDeviation) {
        this.rng = rng;
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    @Override
    public double applyAsDouble(final double v) {
        final double z = rng.nextGaussian() * standardDeviation + mean;
        return v * Math.exp(z);
    }
}
