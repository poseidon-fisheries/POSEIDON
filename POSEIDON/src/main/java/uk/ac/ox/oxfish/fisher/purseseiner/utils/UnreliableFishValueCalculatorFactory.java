package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.DoubleUnaryOperator;

public class UnreliableFishValueCalculatorFactory implements AlgorithmFactory<FishValueCalculator> {
    private AlgorithmFactory<? extends DoubleUnaryOperator> errorOperator;

    public UnreliableFishValueCalculatorFactory(final AlgorithmFactory<? extends DoubleUnaryOperator> errorOperator) {
        this.errorOperator = errorOperator;
    }

    public UnreliableFishValueCalculatorFactory() {
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getErrorOperator() {
        return errorOperator;
    }

    public void setErrorOperator(final AlgorithmFactory<? extends DoubleUnaryOperator> errorOperator) {
        this.errorOperator = errorOperator;
    }

    @Override
    public UnreliableFishValueCalculator apply(final FishState fishState) {
        return new UnreliableFishValueCalculator(
            fishState.getBiology(),
            errorOperator.apply(fishState)
        );
    }
}
