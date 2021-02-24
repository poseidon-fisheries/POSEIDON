package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class GlobalSetAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctHoldAvailableModulationFunction;
    private final DoubleUnaryOperator pctSetsRemainingModulationFunction;

    public GlobalSetAttractionModulator(
        final double pctHoldAvailableLogisticMidpoint,
        final double pctHoldAvailableLogisticSteepness,
        final double pctSetsRemainingLogisticMidpoint,
        final double pctSetsRemainingLogisticSteepness
    ) {
        this(
            new LogisticFunction(pctHoldAvailableLogisticMidpoint, pctHoldAvailableLogisticSteepness),
            new LogisticFunction(pctSetsRemainingLogisticMidpoint, pctSetsRemainingLogisticSteepness)
        );
    }

    public GlobalSetAttractionModulator(DoubleUnaryOperator pctHoldAvailableModulationFunction, DoubleUnaryOperator pctSetsRemainingModulationFunction) {
        this.pctHoldAvailableModulationFunction = pctHoldAvailableModulationFunction;
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
    }

    @Override
    public double modulate(Fisher fisher) {
        final double modulatedPctHoldAvailable =
            pctHoldAvailableModulationFunction.applyAsDouble(1 - fisher.getHold().getPercentageFilled());
        final double modulatedPctSetsRemaining =
            pctSetsRemainingModulationFunction.applyAsDouble(pctSetsRemaining(fisher));
        return modulatedPctHoldAvailable * modulatedPctSetsRemaining;
    }

    private double pctSetsRemaining(final Fisher fisher) {
        return getFadManager(fisher)
            .getActionSpecificRegulations()
            .getSetLimits()
            .map(reg -> reg.getPctLimitRemaining(fisher))
            .orElse(1.0);
    }

}
