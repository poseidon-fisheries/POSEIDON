package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class GlobalDeploymentAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctActiveFadsLimitModulationFunction;
    private final DoubleUnaryOperator numFadsInStockModulationFunction;

    public GlobalDeploymentAttractionModulator(
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
        // TODO: Needs to be reimplemented for new regulation system.
        throw new RuntimeException("Needs to be reimplemented for new regulation system.");
//        final FadManager fadManager = FadManager.getFadManager(fisher);
//        return fadManager
//            .getActionSpecificRegulations()
//            .getActiveFadLimits()
//            .map(reg -> (double) fadManager.getNumDeployedFads() / reg.getLimit(fisher))
//            .orElse(0.0);
    }
}
