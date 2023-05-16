package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.DoubleUnaryOperator;

public class UnreliableFishValueCalculatorFactory implements AlgorithmFactory<UnreliableFishValueCalculator> {
    private AlgorithmFactory<? extends DoubleUnaryOperator> errorOperatorFactory;

    public UnreliableFishValueCalculatorFactory(final AlgorithmFactory<? extends DoubleUnaryOperator> errorOperatorFactory) {
        this.errorOperatorFactory = errorOperatorFactory;
    }

    public UnreliableFishValueCalculatorFactory() {
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getErrorOperatorFactory() {
        return errorOperatorFactory;
    }

    public void setErrorOperatorFactory(final AlgorithmFactory<? extends DoubleUnaryOperator> errorOperatorFactory) {
        this.errorOperatorFactory = errorOperatorFactory;
    }

    @Override
    public UnreliableFishValueCalculator apply(final FishState fishState) {
        return new UnreliableFishValueCalculator(
            fishState.getBiology(),
            errorOperatorFactory.apply(fishState)
        );
    }
}
