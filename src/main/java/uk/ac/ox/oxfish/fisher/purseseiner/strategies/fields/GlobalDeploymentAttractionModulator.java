package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class GlobalDeploymentAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctActiveFadsLimitModulationFunction;

    public GlobalDeploymentAttractionModulator(
        final double pctActiveFadsLimitLogisticMidpoint,
        final double pctActiveFadsLimitLogisticSteepness
    ) {
        this(
            new LogisticFunction(pctActiveFadsLimitLogisticMidpoint, pctActiveFadsLimitLogisticSteepness)
        );
    }

    private GlobalDeploymentAttractionModulator(
        final DoubleUnaryOperator pctActiveFadsLimitModulationFunction
    ) {
        this.pctActiveFadsLimitModulationFunction = pctActiveFadsLimitModulationFunction;
    }

    @Override
    public double modulate(final Fisher fisher) {
        return pctActiveFadsLimitModulationFunction.applyAsDouble(1 - getPctActiveFads(fisher));
    }

    private double getPctActiveFads(final Fisher fisher) {
        final FadManager fadManager = getFadManager(fisher);
        return fadManager
            .getActionSpecificRegulations()
            .getActiveFadLimits()
            .map(reg -> (double) fadManager.getNumDeployedFads() / reg.getLimit(fisher))
            .orElse(0.0);
    }
}
