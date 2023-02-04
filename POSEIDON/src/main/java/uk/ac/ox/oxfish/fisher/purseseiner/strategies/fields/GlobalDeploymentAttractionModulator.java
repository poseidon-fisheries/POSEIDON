package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

import java.util.function.DoubleUnaryOperator;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;

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
        final FadManager<? extends LocalBiology, ? extends AbstractFad<? extends LocalBiology,? extends AbstractFad<?,?>>> fadManager =
            FadManager.getFadManager(fisher);
        return fadManager
            .getActionSpecificRegulations()
            .getActiveFadLimits()
            .map(reg -> (double) fadManager.getNumDeployedFads() / reg.getLimit(fisher))
            .orElse(0.0);
    }
}
