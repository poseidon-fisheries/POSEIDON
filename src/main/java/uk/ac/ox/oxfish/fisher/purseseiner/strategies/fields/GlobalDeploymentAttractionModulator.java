package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class GlobalDeploymentAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctActiveFadsLimitModulationFunction;
    private final DoubleUnaryOperator numFadsInStockModulationFunction;

    public GlobalDeploymentAttractionModulator(
        final double pctActiveFadsLimitLogisticMidpoint,
        final double pctActiveFadsLimitLogisticSteepness,
        final double numFadsInStockLogisticMidpoint,
        final double numFadsInStockLogisticSteepness
    ) {
        this(
            new LogisticFunction(pctActiveFadsLimitLogisticMidpoint, pctActiveFadsLimitLogisticSteepness),
            new LogisticFunction(numFadsInStockLogisticMidpoint, numFadsInStockLogisticSteepness)
        );
    }

    private GlobalDeploymentAttractionModulator(
        final DoubleUnaryOperator pctActiveFadsLimitModulationFunction,
        final DoubleUnaryOperator numFadsInStockModulationFunction
    ) {
        this.pctActiveFadsLimitModulationFunction = pctActiveFadsLimitModulationFunction;
        this.numFadsInStockModulationFunction = numFadsInStockModulationFunction;
    }

    @Override
    public double modulate(final Fisher fisher) {
        return pctActiveFadsLimitModulationFunction.applyAsDouble(1 - getPctActiveFads(fisher))
            * numFadsInStockModulationFunction.applyAsDouble(getFadManager(fisher).getNumFadsInStock());
    }

    private static double getPctActiveFads(final Fisher fisher) {
        final FadManager fadManager = getFadManager(fisher);
        return fadManager
            .getActionSpecificRegulations()
            .getActiveFadLimits()
            .map(reg -> (double) fadManager.getNumDeployedFads() / reg.getLimit(fisher))
            .orElse(0.0);
    }
}
