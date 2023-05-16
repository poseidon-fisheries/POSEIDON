package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.biology.GlobalBiology;

import java.util.function.DoubleUnaryOperator;

public class UnreliableFishValueCalculator implements FishValueCalculator {

    private final FishValueCalculator delegate;

    private final DoubleUnaryOperator errorOperator;

    public UnreliableFishValueCalculator(
        final GlobalBiology globalBiology,
        final DoubleUnaryOperator errorOperator
    ) {
        this.delegate = new ReliableFishValueCalculator(globalBiology);
        this.errorOperator = errorOperator;
    }

    @Override
    public GlobalBiology getGlobalBiology() {
        return delegate.getGlobalBiology();
    }

    @Override
    public double valueOf(final double[] biomasses, final double[] prices) {
        return errorOperator.applyAsDouble(delegate.valueOf(biomasses, prices));
    }

}
