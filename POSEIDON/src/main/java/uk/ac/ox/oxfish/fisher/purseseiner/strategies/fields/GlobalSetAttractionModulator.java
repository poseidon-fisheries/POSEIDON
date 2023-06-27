package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.function.DoubleUnaryOperator;

public class GlobalSetAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctHoldAvailableModulationFunction;
    private final DoubleUnaryOperator pctSetsRemainingModulationFunction;

    public GlobalSetAttractionModulator(
        final DoubleUnaryOperator pctHoldAvailableModulationFunction,
        final DoubleUnaryOperator pctSetsRemainingModulationFunction
    ) {
        this.pctHoldAvailableModulationFunction = pctHoldAvailableModulationFunction;
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
    }

    @Override
    public double modulate(final Fisher fisher) {
        final double modulatedPctHoldAvailable =
            pctHoldAvailableModulationFunction.applyAsDouble(
                1 - fisher.getHold().getPercentageFilled());
        final double modulatedPctSetsRemaining =
            pctSetsRemainingModulationFunction.applyAsDouble(pctSetsRemaining(fisher));
        return modulatedPctHoldAvailable * modulatedPctSetsRemaining;
    }

    private static double pctSetsRemaining(final Fisher fisher) {
        // TODO: now that set limits are implemented with the unified
        // regulation system, this should be should be adapted to
        // query getFadManager(fisher).getRegulations() and find
        // the appropriate regulation if it exists.
        throw new RuntimeException("Not implemented");
    }

}
