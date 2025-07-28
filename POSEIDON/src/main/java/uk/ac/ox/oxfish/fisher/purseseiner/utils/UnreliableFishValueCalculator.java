package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.biology.GlobalBiology;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

public class UnreliableFishValueCalculator implements FishValueCalculator {

    private final FishValueCalculator delegate;
    private double biomassBlur=0;

    private final DoubleUnaryOperator errorOperator;

    public UnreliableFishValueCalculator(
        final GlobalBiology globalBiology,
        final DoubleUnaryOperator errorOperator
    ) {
        this.delegate = new ReliableFishValueCalculator(globalBiology);
        this.errorOperator = errorOperator;
    }

    public UnreliableFishValueCalculator(
        final GlobalBiology globalBiology,
        final DoubleUnaryOperator errorOperator,
        final double biomassBlur
    ) {
        this.delegate = new ReliableFishValueCalculator(globalBiology);
        this.errorOperator = errorOperator;
        this.biomassBlur = biomassBlur;
    }

    @Override
    public GlobalBiology getGlobalBiology() {
        return delegate.getGlobalBiology();
    }

    @Override
    public double valueOf(final double[] biomasses, final double[] prices) {
        // If blur = 1, then there's no distinction between biomass, we value all fish at the mean price
        // If blur = 0 then we have precise distinction between species under the boats and value them at
        // exact ex vessel prices
        double meanPrice = Arrays.stream(prices).sum()/prices.length;
        for(int i=0; i<prices.length; i++){
            prices[i] = biomassBlur*meanPrice + (1-biomassBlur)*prices[i];
        }
        return errorOperator.applyAsDouble(delegate.valueOf(biomasses, prices));
    }

}
