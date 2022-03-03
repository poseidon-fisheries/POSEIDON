package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

import java.util.function.DoubleUnaryOperator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.CompressedExponentialFunction;

public class GlobalSetAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctHoldAvailableModulationFunction;
    private final DoubleUnaryOperator pctSetsRemainingModulationFunction;

    public GlobalSetAttractionModulator(
        final double pctHoldAvailableCoefficient,
        final double pctHoldAvailableExponent,
        final double pctSetsRemainingCoefficient,
        final double pctSetsRemainingExponent
    ) {
        this(
            new CompressedExponentialFunction(
                pctHoldAvailableCoefficient,
                pctHoldAvailableExponent
            ),
            new CompressedExponentialFunction(pctSetsRemainingCoefficient, pctSetsRemainingExponent)
        );
    }

    private GlobalSetAttractionModulator(
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
        return getFadManager(fisher)
            .getActionSpecificRegulations()
            .getSetLimits()
            .map(reg -> reg.getPctLimitRemaining(fisher))
            .orElse(1.0);
    }

}
